package jsonrpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class Batch { //public solo per test
    private ArrayList<Request> reqs;
    private ArrayList<Response> resps;
    private boolean onlyNotifies;

    public Batch(JSONArray requestArray) { //public solo per test
        setup(requestArray);
    }

    public Batch(ArrayList<Request> requests) { //public solo per test
        JSONArray array = new JSONArray();
        for (Request r : requests) {
            array.put( r == null ? null : r.getObj() );
        }
        setup(array);
    }

    private void setup(JSONArray requestArray) {
        if (requestArray.length() == 0) {throw new InvalidParameterException("Empty array of requests");}
        reqs = new ArrayList<>();
        resps = new ArrayList<>();
        onlyNotifies = true;
        for (int i=0; i<requestArray.length(); i++) {
            Request req = null;
            Response resp = null;
            String stringReq = null;
            try {
                JSONObject o = requestArray.getJSONObject(i);
                stringReq = o.toString();
                req = new Request(stringReq);
                //resp = null;
                if (!req.isNotify()) {onlyNotifies = false;}
            } catch (InvalidParameterException | JSONException e) {
                Id id = stringReq != null ? Id.getIdFromRequest(stringReq) : new Id(); //tenta di recuperarne l'id, altrimenti id null
                Error err = new Error(Error.Errors.INVALID_REQUEST);
                //req = null;
                resp = new Response(id, err);
                onlyNotifies = false;
            } finally {
                reqs.add(req);
                resps.add(resp);
            }
        }
    }
    private void put(Request req, Response resp) {
        int i = reqs.indexOf(req);
        resps.set(i, resp);
    }
    public void put(ArrayList<Response> responses) { //public solo per teset
        //devono essere passate le risposte in numero esatto (pari al numero di richieste non notifiche valide)

        int c; //conta le richieste a cui non va inserita la risposta corrispondente perché non valide o notifiche
        int i;
        for (i = 0, c = 0; i < responses.size() + c; i++) {
            Request req = reqs.get(i); //IndexOutOfBoundsException se le risposte sono troppe
            if (req == null || req.isNotify()) {
                //la risposta ad una richiesta non valida o notifica non deve esserci
                c++;
            } else {
                this.put(req, responses.get(i-c));
            }
        }
        for (; i < reqs.size(); i++) {
            //se ci sono ancora richieste non notifiche a cui non è stata assegnata una risposta
            if (reqs.get(i)!=null && !reqs.get(i).isNotify()) {throw new IndexOutOfBoundsException("Not enough responses");}
            //troppe poche risposte
        }
    }

    void put(JSONArray responses) {
        ArrayList<Response> resps = new ArrayList<>();
        for (int i = 0; i<responses.length(); i++) {
            try {
                resps.add(new Response(responses.get(i).toString()));
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
        }
        this.put(resps);
    }

    public ArrayList<Request> getAllRequests() {
        return reqs;
    } //solo per test

    public ArrayList<Request> getValidRequests() { //public solo per test
        ArrayList<Request> rq = new ArrayList<>();
        for (Request r : reqs) {
            if (r!=null) {
                rq.add(r);
            }
        }
        return rq;
    }

    public ArrayList<Response> getAllResponses() {
        return resps;
    } //solo per test

    public ArrayList<Response> getValidResponses() { //public solo per test
        ArrayList<Response> rp = new ArrayList<>();
        for (Response r : resps) {
            if (r!=null) {
                rp.add(r);
            }
        }
        return rp;
    }

    public String getResponseJSON() { //public solo per test
        JSONArray arr = new JSONArray();
        for (Response r : resps) {
            if (r != null) { //le risposte alle notifiche non vengono inviate
                arr.put(r.getObj());
            }
        }
        return arr.toString();
    }

    public String getRequestJSON() { //public solo per test
        JSONArray arr = new JSONArray();
        for (Request r : reqs) {
            arr.put(r.getObj());
        }
        return arr.toString();
    }

    public boolean isOnlyNotifies() {
        return onlyNotifies;
    }
}
