import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpaceportTransitNetwork implements Serializable {
    static final long serialVersionUID = 666L;

    double shuttleSpeed;
    final double walkSpeed = 1000 / 6.0;

    Station start, end;
    List<ShuttleCorridor> corridors;
    Station origin, destination;

    public String content;
    public String fileContent;

    public SpaceportTransitNetwork() {
        content = "";
        fileContent = "";
        corridors = new ArrayList<>();
    }

    public SpaceportTransitNetwork(String textOrFile) {
        this();
        if (textOrFile != null) readInput(textOrFile);
    }

    private static String cleanText(String value) {
        if (value == null) return "";
        return value.replace("\uFEFF", "").replace("\u0000", "");
    }

    private void cacheSource(String value) {
        content = cleanText(value);
        fileContent = content;
    }

    private String sourceText() {
        if (fileContent != null && !fileContent.isEmpty()) {
            fileContent = cleanText(fileContent);
            if (content == null || content.isEmpty()) content = fileContent;
            return fileContent;
        }
        if (content != null && !content.isEmpty()) {
            content = cleanText(content);
            if (fileContent == null || fileContent.isEmpty()) fileContent = content;
            return content;
        }
        return "";
    }

    private static String decode(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xef
                && (bytes[1] & 0xff) == 0xbb
                && (bytes[2] & 0xff) == 0xbf) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xfe) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xff) == 0xfe && (bytes[1] & 0xff) == 0xff) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }

        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        int nulCount = 0;
        for (int i = 0; i < utf8.length(); i++) if (utf8.charAt(i) == '\0') nulCount++;
        if (nulCount <= utf8.length() / 10) return utf8;

        String littleEndian = new String(bytes, StandardCharsets.UTF_16LE);
        String bigEndian = new String(bytes, StandardCharsets.UTF_16BE);
        return decodeScore(littleEndian) >= decodeScore(bigEndian) ? littleEndian : bigEndian;
    }

    private static int decodeScore(String value) {
        int score = 0;
        String[] keys = {
                "num_shuttle_corridors",
                "origin_point",
                "destination_point",
                "average_shuttle_speed",
                "corridor_name",
                "corridor_stations"
        };
        for (String key : keys) if (value.contains(key)) score += 1000;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c >= 32 && c <= 126) || c == '\n' || c == '\r' || c == '\t') score++;
        }
        return score;
    }

    private Matcher assignment(String variable, String valuePattern) {
        Pattern pattern = Pattern.compile("(?s)(?<![A-Za-z0-9_])"
                + Pattern.quote(variable)
                + "(?![A-Za-z0-9_])\\s*=\\s*"
                + valuePattern);
        Matcher matcher = pattern.matcher(sourceText());
        if (!matcher.find()) throw new IllegalArgumentException("Missing variable: " + variable);
        return matcher;
    }

    public int getIntVar(String varName) {
        return Integer.parseInt(assignment(varName, "([+-]?[0-9]+)").group(1));
    }

    public int getInt(String varName) {
        return getIntVar(varName);
    }

    public String getStringVar(String varName) {
        String escapedName = Pattern.quote(varName);
        String source = sourceText();
        String[] patterns = {
                "(?s)(?<![A-Za-z0-9_])" + escapedName + "(?![A-Za-z0-9_])\\s*=\\s*\\\"([^\\\"]*)\\\"",
                "(?s)(?<![A-Za-z0-9_])" + escapedName + "(?![A-Za-z0-9_])\\s*=\\s*'([^']*)'",
                "(?m)(?<![A-Za-z0-9_])" + escapedName + "(?![A-Za-z0-9_])\\s*=\\s*([^\\r\\n]+)"
        };

        for (String rawPattern : patterns) {
            Matcher matcher = Pattern.compile(rawPattern).matcher(source);
            if (matcher.find()) return matcher.group(1).trim();
        }

        throw new IllegalArgumentException("Missing variable: " + varName);
    }

    public String getString(String varName) {
        return getStringVar(varName);
    }

    public Double getDoubleVar(String varName) {
        String number = "([+-]?(?:(?:[0-9]+(?:\\.[0-9]*)?)|(?:\\.[0-9]+))(?:[eE][+-]?[0-9]+)?)";
        return Double.parseDouble(assignment(varName, number).group(1));
    }

    public double getDouble(String varName) {
        return getDoubleVar(varName);
    }

    public Point getPointVar(String varName) {
        Matcher matcher = assignment(varName, "\\(\\s*([+-]?[0-9]+)\\s*,\\s*([+-]?[0-9]+)\\s*\\)");
        return new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    public Point getPoint(String varName) {
        return getPointVar(varName);
    }

    private List<Point> parsePointList(String value) {
        List<Point> points = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\(\\s*([+-]?[0-9]+)\\s*,\\s*([+-]?[0-9]+)\\s*\\)").matcher(value);
        while (matcher.find()) {
            points.add(new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
        }
        return points;
    }

    private void appendCorridor(List<ShuttleCorridor> target, String corridorName, String stationText) {
        if (corridorName == null || stationText == null) return;
        List<Point> points = parsePointList(stationText);
        if (points.size() < 2) return;

        List<Station> lineStations = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            lineStations.add(new Station(points.get(i), corridorName + " Station " + (i + 1)));
        }
        target.add(new ShuttleCorridor(corridorName, lineStations));
    }

    public List<ShuttleCorridor> getShuttleCorridors() {
        String source = sourceText();
        List<String> names = collectCorridorNames(source);
        List<String> stationBlocks = collectStationBlocks(source);
        int count = Math.min(names.size(), stationBlocks.size());

        try {
            count = Math.min(count, getIntVar("num_shuttle_corridors"));
        } catch (Exception ignored) {
        }

        List<ShuttleCorridor> parsed = new ArrayList<>();
        for (int i = 0; i < count; i++) appendCorridor(parsed, names.get(i), stationBlocks.get(i));
        corridors = parsed;
        return parsed;
    }

    private List<String> collectCorridorNames(String source) {
        List<String> names = new ArrayList<>();
        collectAll(names, source, "(?s)(?<![A-Za-z0-9_])corridor_name(?![A-Za-z0-9_])\\s*=\\s*\\\"([^\\\"]*)\\\"");
        if (names.isEmpty()) {
            collectAll(names, source, "(?s)(?<![A-Za-z0-9_])corridor_name(?![A-Za-z0-9_])\\s*=\\s*'([^']*)'");
        }
        return names;
    }

    private List<String> collectStationBlocks(String source) {
        List<String> stationBlocks = new ArrayList<>();
        String point = "\\(\\s*[+-]?[0-9]+\\s*,\\s*[+-]?[0-9]+\\s*\\)";
        collectAll(stationBlocks, source, "(?s)(?<![A-Za-z0-9_])corridor_stations(?![A-Za-z0-9_])\\s*=\\s*((?:" + point + "\\s*)+)");
        return stationBlocks;
    }

    private void collectAll(List<String> values, String source, String rawPattern) {
        Matcher matcher = Pattern.compile(rawPattern).matcher(source);
        while (matcher.find()) values.add(matcher.group(1));
    }

    public List<ShuttleCorridor> parseCorridors() {
        return getShuttleCorridors();
    }

    public void readInput(String input) {
        try {
            Path path = Paths.get(input);
            cacheSource(Files.exists(path) ? decode(Files.readAllBytes(path)) : input);
        } catch (Exception e) {
            cacheSource(input);
        }

        Point originPoint = getPointVar("origin_point");
        Point destinationPoint = getPointVar("destination_point");

        start = new Station(originPoint, "Origin Point");
        end = new Station(destinationPoint, "Destination Point");
        origin = start;
        destination = end;
        shuttleSpeed = getDoubleVar("average_shuttle_speed") * 1000.0 / 60.0;
        corridors = getShuttleCorridors();
    }
}
