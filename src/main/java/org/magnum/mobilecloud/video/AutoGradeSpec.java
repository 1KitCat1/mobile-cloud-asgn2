package org.magnum.mobilecloud.video;

import autograder.client.Handin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.magnum.autograder.junit.ConsoleFormatter;
import io.magnum.autograder.junit.EvaluationCriteriaScore;
import io.magnum.autograder.junit.JUnitEvaluation;
import io.magnum.autograder.junit.JUnitEvaluator;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpRequest;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.magnum.mobilecloud.integration.test.InternalAutoGradingTest;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.Repository;

public class AutoGradeSpec {
    private static final String GET = "GET";

    public AutoGradeSpec() {
    }

    public static void disableAllLogging() {
        Logger globalLogger = Logger.getLogger("global");
        Handler[] handlers = globalLogger.getHandlers();
        Handler[] var5 = handlers;
        int var4 = handlers.length;

        for(int var3 = 0; var3 < var4; ++var3) {
            Handler handler = var5[var3];
            globalLogger.removeHandler(handler);
        }

        List<org.apache.log4j.Logger> loggers = Collections.list(LogManager.getCurrentLoggers());
        loggers.add(LogManager.getRootLogger());
        Iterator var9 = loggers.iterator();

        while(var9.hasNext()) {
            org.apache.log4j.Logger logger = (org.apache.log4j.Logger)var9.next();
            logger.setLevel(Level.OFF);
        }

        org.apache.log4j.Logger.getLogger("org").setLevel(Level.INFO);
        org.apache.log4j.Logger.getLogger("com").setLevel(Level.INFO);
        org.apache.log4j.Logger.getLogger("io").setLevel(Level.INFO);
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("ROOT");
        root.setLevel(ch.qos.logback.classic.Level.INFO);
    }

    public static void grade(String[] args) throws Exception {
        disableAllLogging();
        File f = new File("./src/main/java/org/magnum/mobilecloud/video/Application.java");
        if (!f.exists()) {
            System.out.println(WordUtils.wrap("You must run the AutoGrading application from the root of the project directory containing src/main/java. If you right-click->Run As->Java Application in Eclipse, it will automatically use the correct classpath and working directory (assuming that you have Gradle and the project setup correctly).", 80));
            System.exit(1);
        }

        File submissionPackages = new File("submission-packages");
        if (!submissionPackages.exists()) {
            submissionPackages.mkdirs();
        }

        try {
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(new HttpHost("127.0.0.1", 8080), new BasicHttpRequest("GET", "/"));
            response.getStatusLine();
        } catch (NoHttpResponseException var21) {
        } catch (Exception var22) {
            System.out.println(WordUtils.wrap("Unable to connect to your server on https://localhost:8080. Are you sure the server is running? In order to run the autograder, you must first launch your application by right-clicking on the Application class in Eclipse, andchoosing Run As->Java Application. If you have already done this, make sure that you can access your server by opening the https://localhost:8080 url in a browser. If you can't access the server in a browser, it probably indicates you have a firewall or some other issue that is blocking access to port 8080 on localhost.", 80));
            System.exit(1);
        }

        JUnitEvaluator eval = new JUnitEvaluator(InternalAutoGradingTest.class);
        JUnitEvaluation estimatedScore = eval.evaluate(new ConsoleFormatter());
        System.out.println(estimatedScore.getFeedback());
        System.out.println("\n\n\nYour solution grade is above.");
        System.out.println("\n\n\nGenerating your submission package...");
        System.out.println("\n");
        Handin h = new Handin();
        Map spec = h.loadSpec("assignment.edn");
        double possible = 0.0D;
        double total = 0.0D;
        Map results = new HashMap();

        EvaluationCriteriaScore score;
        for(Iterator var13 = estimatedScore.getCriteriaScores().iterator(); var13.hasNext(); possible += score.getCriteria().getRubric().points()) {
            score = (EvaluationCriteriaScore)var13.next();
            results.put(score.getCriteria().getClass().getName() + "." + score.getCriteria().getMethod().getName(), score.getScore());
            total += score.getScore();
        }

        Map scoreAndFeedback = new HashMap();
        scoreAndFeedback.put("fractionalScore", 1.0);
        scoreAndFeedback.put("feedback", estimatedScore.getFeedback());
        scoreAndFeedback.put("asgn", "asgn2");
        String scoreAndFeedbackJson = (new ObjectMapper()).writeValueAsString(scoreAndFeedback);
        Map metadata = new HashMap();
        Map result = h.submitResults(spec, "id-token", metadata, results, scoreAndFeedbackJson);
        boolean success = (Boolean)result.get("success");
        (new StringBuilder()).append(result.get("msg")).toString();
        LocalDateTime ldt = LocalDateTime.now();
        String timestr = DateTimeFormatter.ofPattern("MM_dd_yyyy__hh_mm_ss", Locale.ENGLISH).format(ldt);
        File pkg = new File(submissionPackages, timestr + ".pkg");
        FileUtils.writeStringToFile(pkg, result.get("submission-package").toString());
        System.out.println("Your submission package was saved to: \n\n" + pkg.getAbsolutePath());
    }

    private static boolean isJpaRepository(Class<?> clazz) {
        if (clazz.isInterface() && Repository.class.equals(clazz)) {
            return true;
        } else {
            boolean isRepository = false;
            Class[] var5;
            int var4 = (var5 = clazz.getInterfaces()).length;

            for(int var3 = 0; var3 < var4; ++var3) {
                Class<?> c = var5[var3];
                isRepository = isRepository || isJpaRepository(c);
            }

            return isRepository;
        }
    }
    public static void main(String[] args) {
        try {
            grade(args);
        } catch (Exception ex) {}
    }
}
