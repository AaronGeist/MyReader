package aaron.geist.myreader.loader;

import android.os.AsyncTask;
import android.util.Log;
import aaron.geist.myreader.constant.LoaderConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

/**
 * Created by yzhou7 on 2015/7/21.
 */
public class AsyncPageLoader extends AsyncTask<URL, Integer, Document> {

    public AsyncPageLoaderResponse asyncPageLoaderResponse = null;

    @Override
    protected Document doInBackground(URL... urls) {
        Document result = null;
        if (urls.length > 0) {
            result = loadSingleWebPage(urls[0]);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Document doc) {
        Log.d("", "finish AsyncPageLoader");
        asyncPageLoaderResponse.onTaskCompleted(doc);
    }

    /**
     * This method will load web page and deal with Jsoup to get the content we want.
     * @param url
     * @return
     */
    private Document loadSingleWebPage(URL url) {
        Log.d("", "start loadSingleWebPage");
        Document doc = null;

        // use jsoup to load url
        try {
            doc = Jsoup.parse(url, LoaderConstants.DEFAULT_LOAD_TIMEOUT_MILLISEC);
        } catch (IOException e) {
            Log.d("", "IOException: "  + e.getMessage());
        }

        // remove useless tag blocks
        if (doc != null) {
            Elements es = doc.select("script");
            if (es != null) {
                es.remove();
            }
        }

        Log.d("", "finish loadSingleWebPage");
        return doc;
    }
}
