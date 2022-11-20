package com.wust.endpoint.chain.quetsal.query;

import com.fluidops.fedx.Config;
import com.fluidops.fedx.DefaultEndpointListProvider;
import com.fluidops.fedx.FedXFactory;
import com.fluidops.fedx.sail.FedXSailRepository;
import com.fluidops.fedx.structures.QueryInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

@Slf4j
public class QueryEvaluation {

    QueryProvider qp;

    public QueryEvaluation() throws Exception {
        qp = new QueryProvider("./queries/");
    }

    static Map<String, List<List<Object>>> multyEvaluate(String queries, int num, String cfgName, List<String> endpoints) throws Exception {
        QueryEvaluation qeval = new QueryEvaluation();

        Map<String, List<List<Object>>> result = null;
        for (int i = 0; i < num; ++i) {
            Map<String, List<List<Object>>> subReports = qeval.evaluate(queries, cfgName, endpoints);
            if (i == 0) {
                result = subReports;
            } else {
                //assert(report.size() == subReport.size());
                for (Map.Entry<String, List<List<Object>>> e : subReports.entrySet())
                {
                    List<List<Object>> subReport = e.getValue();
                    for (int j = 0; j < subReport.size(); ++j) {
                        List<Object> subRow = subReport.get(j);
                        List<Object> row = result.get(e.getKey()).get(j);
                        row.add(subRow.get(2));
                    }
                }
            }
        }


        return result;
    }

    public Map<String, List<List<Object>>> evaluate(String queries, String cfgName, List<String> endpoints) throws Exception {
        List<List<Object>> report = new ArrayList<List<Object>>();
        List<List<Object>> sstreport = new ArrayList<List<Object>>();
        Map<String, List<List<Object>>> result = new HashMap<String, List<List<Object>>>();
        result.put("report", report);
        result.put("sstreport", sstreport);

        List<String> qnames = Arrays.asList(queries.split(" "));
        for (String curQueryName : qnames)
        {
            List<Object> reportRow = new ArrayList<Object>();
            report.add(reportRow);
            String curQuery = qp.getQuery(curQueryName);
            reportRow.add(curQueryName);

            List<Object> sstReportRow = new ArrayList<Object>();
            sstreport.add(sstReportRow);
            sstReportRow.add(curQueryName);

            Config config = new Config(cfgName);
            SailRepository repo = null;
            TupleQueryResult res = null;

            try {
                repo = FedXFactory.initializeSparqlFederation(config, endpoints);
                TupleQuery query = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, curQuery);

                long startTime = System.currentTimeMillis();
                res = query.evaluate();
                long count = 0;

                while (res.hasNext()) {
                    BindingSet row = res.next();
                    System.out.println(count+": "+ row);
                    count++;
                }

                long runTime = System.currentTimeMillis() - startTime;
                reportRow.add((Long)count); reportRow.add((Long)runTime);
                sstReportRow.add((Long)count);
                sstReportRow.add(QueryInfo.queryInfo.get().numSources.longValue());
                sstReportRow.add(QueryInfo.queryInfo.get().totalSources.longValue());
                log.info(curQueryName + ": Query exection time (msec): "+ runTime + ", Total Number of Records: " + count + ", Source count: " + QueryInfo.queryInfo.get().numSources.longValue());
                log.info(curQueryName + ": Query exection time (msec): "+ runTime + ", Total Number of Records: " + count + ", Source Selection Time: " + QueryInfo.queryInfo.get().getSourceSelection().time);
            } catch (Throwable e) {
                e.printStackTrace();
                log.error("", e);
                File f = new File("results/" + curQueryName + ".error.txt");
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(os);
                e.printStackTrace(ps);
                ps.flush();
                FileUtils.write(f, os.toString("UTF8"));
                reportRow.add(null); reportRow.add(null);
            } finally {
                if (null != res) {
                    res.close();
                }

                if (null != repo) {
                    repo.shutDown();
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        String cfgName = args[0];
        String host = "localhost";
        List<String> endpointsMin = Arrays.asList(
                //"http://" + host + ":8890/sparql",
                "http://" + host + ":8892/sparql",
                "http://" + host + ":8895/sparql"
        );
        String queries = "C1";
        multyEvaluate(queries,1,cfgName,endpointsMin);
    }

}
