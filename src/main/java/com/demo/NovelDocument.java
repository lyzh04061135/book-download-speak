package com.demo;

import com.demo.utils.CommonUtil;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.*;

@Data
public class NovelDocument {
    private String url;
    private String chapterRule;
    private String contentRule;
    private String path;
    private String charset;

    public NovelDocument(String url, String chapterRule, String contentRule, String path, String charset) {
        this.url=url;
        this.chapterRule=chapterRule;
        this.contentRule=contentRule;
        this.path=path;
        this.charset=charset;
    }

    public boolean download() {
        if (CommonUtil.isBlank(url)) {
            System.out.println("url is empty!");
            return false;
        }
        if (CommonUtil.isBlank(path)) {
            System.out.println("path is empty!");
            return false;
        }

        getDefaultRule();

        if (CommonUtil.isBlank(chapterRule)) {
            System.out.println("url is empty!");
            return false;
        }
        if (CommonUtil.isBlank(contentRule)) {
            System.out.println("contentRule is empty!");
            return false;
        }

        if (CommonUtil.isBlank(charset)) {
            charset="utf-8";
        }

        boolean flag=true;

        //download chapter list
        String html=null;
        try {
            html=download(url);
            if (CommonUtil.isBlank(html)) {
                System.out.println(url);
                return false;
            }
        } catch (Exception e) {
            System.out.println(url);
            e.printStackTrace();
            return false;
        }

        //download chapters
        Document document=Jsoup.parse(html);
        Elements elements=document.select(chapterRule);
        List<List<String>>list=new ArrayList<>();
        Map<String, String>map=new HashMap<>();
        for(int i=0; i<elements.size(); i++) {
            try {
                Element element=elements.get(i);
                String chapterUrl=getChapterUrl(element);
                String chapterTile=element.text();
                if (CommonUtil.isBlank(chapterUrl)) {
                    System.out.println(String.format("warn chapter %d", i));
                }
                else {
                    if (chapterTile==null) {
                        chapterTile="";
                    }
                    List<String>urlAndTile=new ArrayList<>();
                    urlAndTile.add(chapterUrl);
                    urlAndTile.add(chapterTile);
                    if(map.get(chapterUrl)==null) {
                        map.put(chapterUrl, chapterTile);
                    }
                    else {
                        for(int j=0; j<list.size(); j++) {
                            if (list.get(j).get(0).equals(chapterUrl)) {
                                list.remove(j);
                                break;
                            }
                        }
                    }
                    list.add(urlAndTile);
                }
            }
            catch (Exception e) {
                System.out.println(String.format("element i: %d fail!", i));
                e.printStackTrace();
            }
        }

        BufferedWriter bufferedWriter=null;

        try {
            bufferedWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        } catch (Exception e) {
            System.out.println(String.format("open file %s fail!", path));
            e.printStackTrace();
            return false;
        }

        //log
        for(int i=0; i<list.size(); i++) {
            System.out.println(String.format("%s, %s", list.get(i).get(0), list.get(i).get(1)));
        }

        System.out.println("-------------------------------");

        for(int i=0; i<list.size(); i++) {
            try {
                html=download(list.get(i).get(0));
                if (CommonUtil.isBlank(html)) {
                    System.out.println(String.format("warn %s, %s", list.get(i).get(0), list.get(i).get(1)));
                    continue;
                }
                document=Jsoup.parse(html);
                Element element=document.selectFirst(contentRule);
                String content=element.text();
                if (charset.toLowerCase().equals("utf-8") || charset.toLowerCase().equals("utf8")) {
                    bufferedWriter.write(list.get(i).get(1));
                    bufferedWriter.write("\r\n");
                    bufferedWriter.write(content);
                }
                else {
                    bufferedWriter.write(new String(list.get(i).get(1).getBytes(charset), "utf-8"));
                    bufferedWriter.write("\r\n");
                    bufferedWriter.write(new String(content.getBytes(charset), "utf-8"));
                }
                bufferedWriter.write("\r\n");
                bufferedWriter.flush();
            } catch (Exception e) {
                System.out.println(String.format("chapter url: %s, chapter title: %s", list.get(i).get(0), list.get(i).get(1)));
                e.printStackTrace();
            }
        }

        try {
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(String.format("close file %s fail!", path));
            e.printStackTrace();
            return false;
        }

        return flag;
    }

    private void getDefaultRule() {
        if (CommonUtil.isBlank(chapterRule)) {
            if (url.startsWith("https://www.biqukan.com")) {
                chapterRule="body > div.listmain > dl > dd";
            }
        }

        if (CommonUtil.isBlank(contentRule)) {
            if (url.startsWith("https://www.biqukan.com")) {
                contentRule="#content";
            }
        }
    }

    private String getChapterUrl(Element element) throws Exception {
        URI uri=new URI(url);
        Element a=element.selectFirst("a");
        String url=a.attr("href");
        uri=uri.resolve(url);
        url=uri.toURL().toString();
        return url;
    }

    private String download(String url) throws Exception{
        String html=null;
        for(int i=0; i<3; i++) {
            try {
                Map<String, String>headers=new HashMap<>();
                headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
                headers.put("Hm_lvt_d980a3f9499907d0586dbac4f3207804", ""+new Date().getTime());
                Connection connection=Jsoup.connect(url).timeout(5000).headers(headers);
                Thread.sleep(300);
                Document document=connection.get();
                html=document.outerHtml();
                if (!CommonUtil.isBlank(html)) {
                    break;
                }
                else {
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                System.out.println(url);
                e.printStackTrace();
                Thread.sleep(10000);
            }
        }
        return html;
    }

}
