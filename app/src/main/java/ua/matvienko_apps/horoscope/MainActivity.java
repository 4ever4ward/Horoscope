package ua.matvienko_apps.horoscope;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private String HForecastString;

    private final String TAURUS = "taurus";
    private final String GEMINI = "gemini";
    private final String TODAY = "today";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread() {
            @Override
            public void run() {
                Sync();

                try {
                    Log.e(TAG, "run: " + getTodayForecast(HForecastString, GEMINI, TODAY));
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    e.printStackTrace();
                }


            }
        }.start();

    }

    public void Sync() {
        HttpURLConnection urlConnection;
        BufferedReader reader;

        URL url = null;

        try {

            url = new URL("http://img.ignio.com/r/export/utf/xml/daily/com.xml");
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return;
                }
                HForecastString = buffer.toString();
                Log.e(TAG, HForecastString);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getTodayForecast(String forecastString, String zodiacSign, String day) throws ParserConfigurationException, IOException, SAXException {
        String todayForecasts = null;

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(forecastString));

        Document doc = db.parse(is);

        Element element = doc.getDocumentElement();
        element.normalize();

        NodeList nodeList = doc.getElementsByTagName(zodiacSign);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element2 = (Element) node;

                todayForecasts = getValue(day, element2);

            }
        }
        return todayForecasts;
    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
}
