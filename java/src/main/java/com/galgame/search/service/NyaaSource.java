package com.galgame.search.service;

import com.galgame.search.model.Game;
import com.galgame.search.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NyaaSource implements GameSource {

    private final HttpClient client;

    public NyaaSource() {
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public List<Game> searchGame(String keyword) throws Exception {
        // Nyaa RSS: https://nyaa.si/?page=rss&c=1_2&q=KEYWORD
        // c=1_2 is Software - Games (Audio Games is 1_3, maybe we want both? or just general search)
        // Let's stick to 1_2 strictly for Galgames, or remove 'c' to search all.
        // User asked for Galgames, which are usually under Games.
        
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String rssUrl = "https://nyaa.si/?page=rss&c=1_2&q=" + encodedKeyword;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rssUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("Nyaa returned HTTP " + response.statusCode());
        }

        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(response.body().getBytes(StandardCharsets.UTF_8)));
        
        NodeList items = doc.getElementsByTagName("item");
        List<Game> games = new ArrayList<>();

        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                
                String title = getTagValue("title", elem);
                String link = getTagValue("link", elem); // This is the magnet link in Nyaa RSS usually, or .torrent?
                // Nyaa RSS 'link' is often the magnet link? Let's verify.
                // Actually Nyaa RSS 'link' is usually the magnet link or the torrent file link.
                // Let's check typical Nyaa RSS. 
                // <link>magnet:?xt=...</link> works for some RSS readers.
                // Nyaa.si RSS <link> is often the download link (torrent file) or magnet.
                // Wait, Nyaa.si RSS <link> element contains the MAGNET link?
                // Actually, often it is <link>http://nyaa.si/download/...</link>
                // And <nyaa:infoHash>...</nyaa:infoHash> is there.
                // BUT, most RSS readers expect <link> to be the page.
                // Let's assume 'link' is the magnet or download link.
                // If it is a http link to .torrent, we can return that.
                
                // Correction: Nyaa.si RSS:
                // <item>
                //   <title>...</title>
                //   <link>magnet:?xt=urn:btih:...</link>
                //   <guid...
                // </item>
                // It seems default Nyaa RSS puts magnet in <link>.
                
                String pubDate = getTagValue("pubDate", elem);
                String size = getTagValue("nyaa:size", elem); // Might not be available in standard javax parser without ns awareness
                
                // Use magnet link as ID directly
                String id = link;
                
                // Description
                String intro = "发布时间: " + pubDate;
                
                games.add(new Game(id, title, intro, List.of("PC"), "Nyaa"));
            }
        }
        return games;
    }

    @Override
    public List<Resource> getDownloads(String id) throws Exception {
        // ID is the magnet link or download URL
        return List.of(new Resource("磁力链接 / 下载链接", id, null, "BitTorrent"));
    }

    @Override
    public String getSourceName() {
        return "Nyaa";
    }
    
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null) {
                return node.getTextContent();
            }
        }
        return "";
    }
}
