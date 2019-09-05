package com.example.rulereasoning.service;
import com.example.rulereasoning.dao.TripleDao;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleReasonerService {

    @Autowired
    private TripleDao tripleDao;

    public String reason(){

        Model model = ModelFactory.createDefaultModel();
        ResultSet rs = tripleDao.getTriples();
        while (rs.hasNext()) {

            QuerySolution qs = rs.next() ;

            String subject = qs.get("s").toString();

            String object = qs.get("o").toString();

            String predicate = qs.get("p").toString();

            if(predicate.contains("contains")) {
                model.add(model.createResource(subject), model.createProperty("contains"), model.createResource(object));
            }
            else if(predicate.contains("provides")) {
                model.add(model.createResource(subject), model.createProperty("provides"), model.createResource(object));
            }
        }
        String userDefinedRules = tripleDao.getRule();
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(userDefinedRules));
        reasoner.setDerivationLogging(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        return TriplesToJson(inf.getDeductionsModel());
    }

    private void outputAllTriples(Model model) {
        StmtIterator itr = model.listStatements();
        while (itr.hasNext()) {
            System.out.println(itr.nextStatement());
        }
    }

    //将推理出的三元组写回数据库
    private void updateTriplesInFuseki(Model model) {
        StmtIterator itr = model.listStatements();
        RDFConnectionRemoteBuilder myBuilder = RDFConnectionFuseki.create().destination(tripleDao.getIpAddress());
        try (RDFConnection conn = (RDFConnectionFuseki)myBuilder.build() ){
            while (itr.hasNext()) {
                Statement nowStatement = itr.nextStatement();
                String subject = nowStatement.getSubject().toString();
                String predicate = nowStatement.getPredicate().toString();
                String object = nowStatement.getObject().toString();
                String sentence = "insert {<" + subject + "> <" + subject + "/" + predicate +"> <" + object + ">} where {}";
                System.out.println(sentence);
                conn.update(sentence);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String TriplesToJson(Model model) {
        JSONArray triples = new JSONArray();
        StmtIterator itr = model.listStatements();
        while (itr.hasNext()) {
            Statement nowStatement = itr.nextStatement();
            String subject = nowStatement.getSubject().toString();
            String predicate = nowStatement.getPredicate().toString();
            String object = nowStatement.getObject().toString();
            JSONObject triple = new JSONObject();
            triple.put("subject", subject);
            triple.put("predicate", subject + "/" + predicate);
            triple.put("object", object);
            triples.put(triple);
        }
        return triples.toString();
    }
}
