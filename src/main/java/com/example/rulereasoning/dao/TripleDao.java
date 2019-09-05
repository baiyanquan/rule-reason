package com.example.rulereasoning.dao;
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.stereotype.Component;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Component(value = "TripleDao")
public class TripleDao {

    private String ipAddress;

    private String queryString;

    private String rule;

    public ResultSet getTriples(){

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(ipAddress);

        Query query = QueryFactory.create(queryString);

        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {

            QueryExecution qExec = conn.query(query);

            ResultSet rs = qExec.execSelect();

            return rs;
        }
    }

    public String getRule(){
        return rule;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public TripleDao(){
        Element element = null;
        File f = new File("./info.xml");

        DocumentBuilder db = null;
        DocumentBuilderFactory dbf = null;
        try {
            // 返回documentBuilderFactory对象
            dbf = DocumentBuilderFactory.newInstance();
            // 返回db对象用documentBuilderFatory对象获得返回documentBuildr对象
            db = dbf.newDocumentBuilder();

            // 得到一个DOM并返回给document对象
            Document dt = db.parse(f);

            ipAddress = dt.getElementsByTagName("ipAddress").item(0).getTextContent();

            queryString = dt.getElementsByTagName("queryString").item(0).getTextContent();

            rule = dt.getElementsByTagName("rule").item(0).getTextContent();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
