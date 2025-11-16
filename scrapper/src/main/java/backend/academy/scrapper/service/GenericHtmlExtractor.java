package backend.academy.scrapper.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class GenericHtmlExtractor {

    public Result extract(byte[] htmlBytes, String baseUrl) {
        Document doc = Jsoup.parse(new String(htmlBytes, StandardCharsets.UTF_8), baseUrl);

        doc.select("script,style,noscript,iframe,svg,canvas,video,audio,form,header,footer,nav,aside")
                .remove();
        doc.select(
                        "[class~=(?i)(ad|ads|advert|banner|promo|sponsor|cookie|consent|subscribe|newsletter|share|social|breadcrumbs|login|signup)]")
                .remove();

        String title = Optional.ofNullable(doc.title()).map(String::trim).orElse("");

        Element main = doc.selectFirst("article, main, [itemprop=articleBody], [role=main]");
        if (main == null) {
            main = findContentHeavyDiv(doc.body());
        }
        if (main == null) main = doc.body();

        String text = main.select("p, h1, h2, h3, li").eachText().stream()
                .map(this::normalize)
                .filter(s -> s.length() >= 40)
                .collect(Collectors.joining("\n\n"));

        if (text.isBlank()) text = doc.text();

        if (text.length() > 2000) text = text.substring(0, 2000);

        String compositeHash = sha256(title + "\n\n" + text);
        String sample = text.length() > 600 ? text.substring(0, 600) : text;

        return new Result(compositeHash, sample);
    }

    private Element findContentHeavyDiv(Element root) {
        if (root == null) return null;

        return root.select("div").stream()
                .max(Comparator.comparingInt(div -> div.select("p").text().length()))
                .orElse(root);
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", " ")
                .trim()
                .replaceAll(
                        "(?i)\\b(\\d{1,2}[.:]\\d{2}" + // время hh:mm
                                "|\\d{4}-\\d{2}-\\d{2}"
                                + // ISO дата
                                "|\\d{2}[./]\\d{2}[./]\\d{4}"
                                + // дата dd.mm.yyyy
                                "|\\d+\\s+(minutes?|hours?|days?)\\s+ago)"
                                + // "3 hours ago"
                                "\\b",
                        " ");
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute SHA-256", e);
        }
    }

    public record Result(String compositeHash, String contentSample) {}
}
