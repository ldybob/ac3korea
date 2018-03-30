package com.ldybob.ac3korea;

public class strHelper {
    public static String bbsContent(String a)
    {
        a = entitychar2html(a);
        a = nl2br(a);
        a = bbcode(a);
        return a;
    }

    public static String nl2br(String a) {
        String b = a.replaceAll("\n", "<br>");
        return b;
    }

    public static String bbcode(String a){
        String[] pattern = {"(?i)\\[b\\](.+?)\\[/b\\]",
                "(?i)\\[i\\](.+?)\\[/i\\]",
                "(?i)\\[u\\](.+?)\\[/u\\]",
                "(?i)\\[strike\\](.+?)\\[/strike\\]",
                "(?i)\\[color=#([0-9a-fA-F]{6})\\](.+?)\\[/color\\]",
                "(?i)\\[url=([a-z0-9]+://)([\\w\\-]+\\.([\\w\\-]+\\.)*[\\w]+(:[0-9]+)?(/[^ \\\"\\n\\r\\t<]*?)?)\\](.*?)\\[/url\\]",
                "(?i)\\[url\\]([a-z0-9]+?://){1}([\\w\\-]+\\.([\\w\\-]+\\.)*[\\w]+(:[0-9]+)?(/[^ \\\"\\n\\r\\t<]*)?)\\[/url\\]",
                "(?i)\\[url\\]((www|ftp)\\.([\\w\\-]+\\.)*[\\w]+(:[0-9]+)?(/[^ \\\"\\n\\r\\t<]*?)?)\\[/url\\]"};
        String[] replace = {"<b>$1</b>",
                "<em>$1</em>",
                "<span style=\"border-bottom: 1px dotted\">$1</span>",
                "<strike>$1</strike>",
                "<span style=\"color:#$1;\">$2</span>",
                "<a href=\"$1$2\">$6</a>",
                "<a href=\"$1$2\">$1$2</a>",
                "<a href=\"https://$1\">$1</a>"};
        for (int i = 0; i<pattern.length;i++){
            a = a.replaceAll(pattern[i], replace[i]);
        }
        return a;
    }

    public static String percent(String a) {
        a = a.replace("%", "%25");
        return a;
    }
    public static String entitychar2html(String s) {
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&quot;"); break;
                case 'à': sb.append("&agrave;");break;
                case 'À': sb.append("&Agrave;");break;
                case 'â': sb.append("&acirc;");break;
                case 'Â': sb.append("&Acirc;");break;
                case 'ä': sb.append("&auml;");break;
                case 'Ä': sb.append("&Auml;");break;
                case 'å': sb.append("&aring;");break;
                case 'Å': sb.append("&Aring;");break;
                case 'æ': sb.append("&aelig;");break;
                case 'Æ': sb.append("&AElig;");break;
                case 'ç': sb.append("&ccedil;");break;
                case 'Ç': sb.append("&Ccedil;");break;
                case 'é': sb.append("&eacute;");break;
                case 'É': sb.append("&Eacute;");break;
                case 'è': sb.append("&egrave;");break;
                case 'È': sb.append("&Egrave;");break;
                case 'ê': sb.append("&ecirc;");break;
                case 'Ê': sb.append("&Ecirc;");break;
                case 'ë': sb.append("&euml;");break;
                case 'Ë': sb.append("&Euml;");break;
                case 'ï': sb.append("&iuml;");break;
                case 'Ï': sb.append("&Iuml;");break;
                case 'ô': sb.append("&ocirc;");break;
                case 'Ô': sb.append("&Ocirc;");break;
                case 'ö': sb.append("&ouml;");break;
                case 'Ö': sb.append("&Ouml;");break;
                case 'ø': sb.append("&oslash;");break;
                case 'Ø': sb.append("&Oslash;");break;
                case 'ß': sb.append("&szlig;");break;
                case 'ù': sb.append("&ugrave;");break;
                case 'Ù': sb.append("&Ugrave;");break;
                case 'û': sb.append("&ucirc;");break;
                case 'Û': sb.append("&Ucirc;");break;
                case 'ü': sb.append("&uuml;");break;
                case 'Ü': sb.append("&Uuml;");break;
                case '®': sb.append("&reg;");break;
                case '©': sb.append("&copy;");break;
                case '€': sb.append("&euro;"); break;
                case ' ': sb.append("&nbsp;");break;

                default:  sb.append(c); break;
            }
        }
        return sb.toString();
    }
}
