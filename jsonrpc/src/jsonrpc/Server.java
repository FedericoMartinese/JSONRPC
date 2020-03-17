package jsonrpc;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import zeromq.IZmqServer;
import zeromq.ZmqServer;
import java.util.ArrayList;

public class Server implements IServer {
    private IZmqServer server;
    private Batch currBatch;

    public Server(int port) {
        this.server = new ZmqServer(port);
        this.currBatch = null;
    }

    @Override
    public ArrayList<Request> receive() {
        String receivedString = server.receive();
        try {
            Object json = new JSONTokener(receivedString).nextValue();
            if (json instanceof JSONArray) {
                JSONArray arr = new JSONArray(receivedString);
                if (arr.length() == 0) {throw new JSONRPCException("Request batch array is empty");}
                currBatch = new Batch(arr);
                return currBatch.getValidRequests();
            } else if (json instanceof JSONObject) {
                currBatch = null;
                //una richiesta non batch: arraylist<request> di dimensione 1
                Request req = new Request(receivedString);
                ArrayList<Request> requests = new ArrayList<>();
                requests.add(req);
                return requests;
            } else {
                throw new JSONRPCException("Invalid json received");
            }
        } catch (JSONException | JSONRPCException e) {
            //se json è invalido viene restituita in automatico un'unica risposta con errore di parsing (vedi documentazione)
            //indifferentemente se era una richiesta o un array di richieste
            Id id = Id.getIdFromRequest(receivedString); //se è una richiesta unica tenta di recuperarne l'id, altrimenti id null
            Error err = new Error(Error.Errors.PARSE);
            Response errorResp = new Response(id, err);
            server.reply(errorResp.getJsonString());
            currBatch = null;
        }
        return new ArrayList<>(); //se ci sono errori la lista di richieste da eseguire è vuota
    }

    public void reply(Response response) throws JSONRPCException {
        if (currBatch != null) { throw new JSONRPCException("Batch responses needed");}
        server.reply(response.getJsonString());
    }

    @Override
    public void reply(ArrayList<Response> responses) throws JSONRPCException {
        if (currBatch == null && responses.size() > 1) {
            throw new JSONRPCException("Single response needed");
        }
        if (responses.size() == 0) {
            //nessuna risposta a batch di sole notifiche
            currBatch = null;
        } else if (responses.size() == 1 && currBatch == null) {
            //risposta singola a richiesta singola
            //currBatch == null evita di rispondere con una risposta singola a un batch di richieste di dimensione 1. in questo caso si risponde con un batch di risposte di dimensione 1
            this.reply(responses.get(0));
        } else {
            currBatch.put(responses);
            server.reply(currBatch.getResponseJSON());
            currBatch = null;
        }
    }

}
