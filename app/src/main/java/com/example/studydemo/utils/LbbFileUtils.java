package com.example.studydemo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class LbbFileUtils {

    private static final int BUFF_SIZE = 1024 * 10;


    public static void copy(InputStream inputStream, File output) throws IOException {
        OutputStream outputStream = null;
        if (!output.getParentFile().exists()) {
            output.getParentFile().mkdirs();
        }
        try {
            outputStream = new FileOutputStream(output);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }


    /**
     * 将url进行encode，解决部分手机无法下载含有中文url的文件的问题（如OPPO R9）
     *
     * @param url
     * @return
     */
    public static String toUtf8String(String url) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }


    /**
     * 利用文件url转换出文件名
     *
     * @param url
     * @return
     */
    public static String parseName(String url) {
        String fileName = null;
        try {
            fileName = url.substring(url.lastIndexOf("/") + 1);
        } finally {
            if (TextUtils.isEmpty(fileName)) {
                fileName = String.valueOf(System.currentTimeMillis());
            }
        }
        return fileName;
    }

    public static String getType(String url) {
        String type = null;
        try {
            type = url.substring(url.lastIndexOf(".") + 1);
        } finally {

        }
        return type;
    }


    /**
     * 将字节数转换为KB、MB、GB
     *
     * @param size 字节大小
     * @return
     */
    public static String formatKMGByBytes(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.00");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    public static String parseFormat(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }


    public static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        // 获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名 */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "")
            return type;
        // 在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    public static boolean mkDirs(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        int index = filePath.lastIndexOf(File.separator);
        if (index < 1) {
            return false;
        }
        try {
            File dirFile = new File(filePath.substring(0, index));
            if (dirFile != null && !dirFile.exists()) {
                return dirFile.mkdirs();
            } else if (dirFile != null && dirFile.exists()) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }


    /**
     * 将String数据存为文件(续写)
     */
    public static boolean writeStringToFileAppend(String name, String path) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        byte[] b = name.getBytes();
        BufferedOutputStream stream = null;
        File file;
        try {
            file = new File(path);
            if (!mkDirs(path)) {
                return false;
            }
            FileOutputStream fstream = new FileOutputStream(file, true);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
            stream.write("\n".getBytes());
            return true;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 将String数据存为文件(非续写)
     */
    public static boolean writeStringToFile(String name, String path) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        byte[] b = name.getBytes();
        BufferedOutputStream stream = null;
        File file;
        try {
            file = new File(path);
            if (!mkDirs(path)) {
                return false;
            }
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
            return true;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }


    public static String readFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        byte[] bytes = readFile(new File(filePath));
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }


    public static byte[] readFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[BUFF_SIZE];
            int len = fis.read(bytes);
            while (len > 0) {
                baos.write(bytes, 0, len);
                len = fis.read(bytes);
            }
            fis.close();
            byte[] temp = baos.toByteArray();
            baos.close();
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将文件转成base64 字符串
     *
     * @return
     * @throws Exception
     */
    public static String encodeBase64File(Context context, Uri uri) throws Exception {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[(int) inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            return Base64.encodeToString(buffer, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String[][] MIME_MapTable = {
            // --{后缀名， MIME类型}   --
            {".3gp", "video/3gpp"},
            {".3gpp", "video/3gpp"},
            {".aac", "audio/x-mpeg"},
            {".amr", "audio/x-mpeg"},
            {".apk", "application/vnd.android.package-archive"},
            {".avi", "video/x-msvideo"},
            {".aab", "application/x-authoware-bin"},
            {".aam", "application/x-authoware-map"},
            {".aas", "application/x-authoware-seg"},
            {".ai", "application/postscript"},
            {".aif", "audio/x-aiff"},
            {".aifc", "audio/x-aiff"},
            {".aiff", "audio/x-aiff"},
            {".als", "audio/x-alpha5"},
            {".amc", "application/x-mpeg"},
            {".ani", "application/octet-stream"},
            {".asc", "text/plain"},
            {".asd", "application/astound"},
            {".asf", "video/x-ms-asf"},
            {".asn", "application/astound"},
            {".asp", "application/x-asap"},
            {".asx", " video/x-ms-asf"},
            {".au", "audio/basic"},
            {".avb", "application/octet-stream"},
            {".awb", "audio/amr-wb"},
            {".bcpio", "application/x-bcpio"},
            {".bld", "application/bld"},
            {".bld2", "application/bld2"},
            {".bpk", "application/octet-stream"},
            {".bz2", "application/x-bzip2"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".cal", "image/x-cals"},
            {".ccn", "application/x-cnc"},
            {".cco", "application/x-cocoa"},
            {".cdf", "application/x-netcdf"},
            {".cgi", "magnus-internal/cgi"},
            {".chat", "application/x-chat"},
            {".clp", "application/x-msclip"},
            {".cmx", "application/x-cmx"},
            {".co", "application/x-cult3d-object"},
            {".cod", "image/cis-cod"},
            {".cpio", "application/x-cpio"},
            {".cpt", "application/mac-compactpro"},
            {".crd", "application/x-mscardfile"},
            {".csh", "application/x-csh"},
            {".csm", "chemical/x-csml"},
            {".csml", "chemical/x-csml"},
            {".css", "text/css"},
            {".cur", "application/octet-stream"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".dcm", "x-lml/x-evm"},
            {".dcr", "application/x-director"},
            {".dcx", "image/x-dcx"},
            {".dhtml", "text/html"},
            {".dir", "application/x-director"},
            {".dll", "application/octet-stream"},
            {".dmg", "application/octet-stream"},
            {".dms", "application/octet-stream"},
            {".dot", "application/x-dot"},
            {".dvi", "application/x-dvi"},
            {".dwf", "drawing/x-dwf"},
            {".dwg", "application/x-autocad"},
            {".dxf", "application/x-autocad"},
            {".dxr", "application/x-director"},
            {".ebk", "application/x-expandedbook"},
            {".emb", "chemical/x-embl-dl-nucleotide"},
            {".embl", "chemical/x-embl-dl-nucleotide"},
            {".eps", "application/postscript"},
            {".epub", "application/epub+zip"},
            {".eri", "image/x-eri"},
            {".es", "audio/echospeech"},
            {".esl", "audio/echospeech"},
            {".etc", "application/x-earthtime"},
            {".etx", "text/x-setext"},
            {".evm", "x-lml/x-evm"},
            {".evy", "application/x-envoy"},
            {".exe", "application/octet-stream"},
            {".fh4", "image/x-freehand"},
            {".fh5", "image/x-freehand"},
            {".fhc", "image/x-freehand"},
            {".fif", "image/fif"},
            {".fm", "application/x-maker"},
            {".fpx", "image/x-fpx"},
            {".fvi", "video/isivideo"},
            {".flv", "video/x-msvideo"},
            {".gau", "chemical/x-gaussian-input"},
            {".gca", "application/x-gca-compressed"},
            {".gdb", "x-lml/x-gdb"},
            {".gif", "image/gif"},
            {".gps", "application/x-gps"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".hdf", "application/x-hdf"},
            {".hdm", "text/x-hdml"},
            {".hdml", "text/x-hdml"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".hlp", "application/winhlp"},
            {".hqx", "application/mac-binhex40"},
            {".hts", "text/html"},
            {".ice", "x-conference/x-cooltalk"},
            {".ico", "application/octet-stream"},
            {".ief", "image/ief"},
            {".ifm", "image/gif"},
            {".ifs", "image/ifs"},
            {".imy", "audio/melody"},
            {".ins", "application/x-net-install"},
            {".ips", "application/x-ipscript"},
            {".ipx", "application/x-ipix"},
            {".it", "audio/x-mod"},
            {".itz", "audio/x-mod"},
            {".ivr", "i-world/i-vrml"},
            {".j2k", "image/j2k"},
            {".jad", "text/vnd.sun.j2me.app-descriptor"},
            {".jam", "application/x-jam"},
            {".jnlp", "application/x-java-jnlp-file"},
            {".jpe", "image/jpeg"},
            {".jpz", "image/jpeg"},
            {".jwc", "application/jwc"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".kjx", "application/x-kjx"},
            {".lak", "x-lml/x-lak"},
            {".latex", "application/x-latex"},
            {".lcc", "application/fastman"},
            {".lcl", "application/x-digitalloca"},
            {".lcr", "application/x-digitalloca"},
            {".lgh", "application/lgh"},
            {".lha", "application/octet-stream"},
            {".lml", "x-lml/x-lml"},
            {".lmlpack", "x-lml/x-lmlpack"},
            {".log", "text/plain"},
            {".lsf", "video/x-ms-asf"},
            {".lsx", "video/x-ms-asf"},
            {".lzh", "application/x-lzh "},
            {".m13", "application/x-msmediaview"},
            {".m14", "application/x-msmediaview"},
            {".m15", "audio/x-mod"},
            {".m3u", "audio/x-mpegurl"},
            {".m3url", "audio/x-mpegurl"},
            {".ma1", "audio/ma1"},
            {".ma2", "audio/ma2"},
            {".ma3", "audio/ma3"},
            {".ma5", "audio/ma5"},
            {".man", "application/x-troff-man"},
            {".map", "magnus-internal/imagemap"},
            {".mbd", "application/mbedlet"},
            {".mct", "application/x-mascot"},
            {".mdb", "application/x-msaccess"},
            {".mdz", "audio/x-mod"},
            {".me", "application/x-troff-me"},
            {".mel", "text/x-vmel"},
            {".mi", "application/x-mif"},
            {".mid", "audio/midi"},
            {".midi", "audio/midi"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".mif", "application/x-mif"},
            {".mil", "image/x-cals"},
            {".mio", "audio/x-mio"},
            {".mmf", "application/x-skt-lbs"},
            {".mng", "video/x-mng"},
            {".mny", "application/x-msmoney"},
            {".moc", "application/x-mocha"},
            {".mocha", "application/x-mocha"},
            {".mod", "audio/x-mod"},
            {".mof", "application/x-yumekara"},
            {".mol", "chemical/x-mdl-molfile"},
            {".mop", "chemical/x-mopac-input"},
            {".movie", "video/x-sgi-movie"},
            {".mpn", "application/vnd.mophun.application"},
            {".mpp", "application/vnd.ms-project"},
            {".mps", "application/x-mapserver"},
            {".mrl", "text/x-mrml"},
            {".mrm", "application/x-mrm"},
            {".ms", "application/x-troff-ms"},
            {".mts", "application/metastream"},
            {".mtx", "application/metastream"},
            {".mtz", "application/metastream"},
            {".mzv", "application/metastream"},
            {".nar", "application/zip"},
            {".nbmp", "image/nbmp"},
            {".nc", "application/x-netcdf"},
            {".ndb", "x-lml/x-ndb"},
            {".ndwn", "application/ndwn"},
            {".nif", "application/x-nif"},
            {".nmz", "application/x-scream"},
            {".nokia-op-logo", "image/vnd.nok-oplogo-color"},
            {".npx", "application/x-netfpx"},
            {".nsnd", "audio/nsnd"},
            {".nva", "application/x-neva1"},
            {".oda", "application/oda"},
            {".oom", "application/x-atlasMate-plugin"},
            {".ogg", "audio/ogg"},
            {".pac", "audio/x-pac"},
            {".pae", "audio/x-epac"},
            {".pan", "application/x-pan"},
            {".pbm", "image/x-portable-bitmap"},
            {".pcx", "image/x-pcx"},
            {".pda", "image/x-pda"},
            {".pdb", "chemical/x-pdb"},
            {".pdf", "application/pdf"},
            {".pfr", "application/font-tdpfr"},
            {".pgm", "image/x-portable-graymap"},
            {".pict", "image/x-pict"},
            {".pm", "application/x-perl"},
            {".pmd", "application/x-pmd"},
            {".png", "image/png"},
            {".pnm", "image/x-portable-anymap"},
            {".pnz", "image/png"},
            {".pot", "application/vnd.ms-powerpoint"},
            {".ppm", "image/x-portable-pixmap"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".pqf", "application/x-cprplayer"},
            {".pqi", "application/cprplayer"},
            {".prc", "application/x-prc"},
            {".proxy", "application/x-ns-proxy-autoconfig"},
            {".prop", "text/plain"},
            {".ps", "application/postscript"},
            {".ptlk", "application/listenup"},
            {".pub", "application/x-mspublisher"},
            {".pvx", "video/x-pv-pvx"},
            {".qcp", "audio/vnd.qcelp"},
            {".qt", "video/quicktime"},
            {".qti", "image/x-quicktime"},
            {".qtif", "image/x-quicktime"},
            {".r3t", "text/vnd.rn-realtext3d"},
            {".ra", "audio/x-pn-realaudio"},
            {".ram", "audio/x-pn-realaudio"},
            {".ras", "image/x-cmu-raster"},
            {".rdf", "application/rdf+xml"},
            {".rf", "image/vnd.rn-realflash"},
            {".rgb", "image/x-rgb"},
            {".rlf", "application/x-richlink"},
            {".rm", "audio/x-pn-realaudio"},
            {".rmf", "audio/x-rmf"},
            {".rmm", "audio/x-pn-realaudio"},
            {".rnx", "application/vnd.rn-realplayer"},
            {".roff", "application/x-troff"},
            {".rp", "image/vnd.rn-realpix"},
            {".rpm", "audio/x-pn-realaudio-plugin"},
            {".rt", "text/vnd.rn-realtext"},
            {".rte", "x-lml/x-gps"},
            {".rtf", "application/rtf"},
            {".rtg", "application/metastream"},
            {".rtx", "text/richtext"},
            {".rv", "video/vnd.rn-realvideo"},
            {".rwc", "application/x-rogerwilco"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".s3m", "audio/x-mod"},
            {".s3z", "audio/x-mod"},
            {".sca", "application/x-supercard"},
            {".scd", "application/x-msschedule"},
            {".sdf", "application/e-score"},
            {".sea", "application/x-stuffit"},
            {".sgm", "text/x-sgml"},
            {".sgml", "text/x-sgml"},
            {".shar", "application/x-shar"},
            {".shtml", "magnus-internal/parsed-html"},
            {".shw", "application/presentations"},
            {".si6", "image/si6"},
            {".si7", "image/vnd.stiwap.sis"},
            {".si9", "image/vnd.lgtwap.sis"},
            {".sis", "application/vnd.symbian.install"},
            {".sit", "application/x-stuffit"},
            {".skd", "application/x-koan"},
            {".skm", "application/x-koan"},
            {".skp", "application/x-koan"},
            {".skt", "application/x-koan"},
            {".slc", "application/x-salsa"},
            {".smd", "audio/x-smd"},
            {".smi", "application/smil"},
            {".smil", "application/smil"},
            {".smp", "application/studiom"},
            {".smz", "audio/x-smd"},
            {".sh", "application/x-sh"},
            {".snd", "audio/basic"},
            {".spc", "text/x-speech"},
            {".spl", "application/futuresplash"},
            {".spr", "application/x-sprite"},
            {".sprite", "application/x-sprite"},
            {".sdp", "application/sdp"},
            {".spt", "application/x-spt"},
            {".src", "application/x-wais-source"},
            {".stk", "application/hyperstudio"},
            {".stm", "audio/x-mod"},
            {".sv4cpio", "application/x-sv4cpio"},
            {".sv4crc", "application/x-sv4crc"},
            {".svf", "image/vnd"},
            {".svg", "image/svg-xml"},
            {".svh", "image/svh"},
            {".svr", "x-world/x-svr"},
            {".swf", "application/x-shockwave-flash"},
            {".swfl", "application/x-shockwave-flash"},
            {".t", "application/x-troff"},
            {".tad", "application/octet-stream"},
            {".talk", "text/x-speech"},
            {".tar", "application/x-tar"},
            {".taz", "application/x-tar"},
            {".tbp", "application/x-timbuktu"},
            {".tbt", "application/x-timbuktu"},
            {".tcl", "application/x-tcl"},
            {".tex", "application/x-tex"},
            {".texi", "application/x-texinfo"},
            {".texinfo", "application/x-texinfo"},
            {".tgz", "application/x-tar"},
            {".thm", "application/vnd.eri.thm"},
            {".tif", "image/tiff"},
            {".tiff", "image/tiff"},
            {".tki", "application/x-tkined"},
            {".tkined", "application/x-tkined"},
            {".toc", "application/toc"},
            {".toy", "image/toy"},
            {".tr", "application/x-troff"},
            {".trk", "x-lml/x-gps"},
            {".trm", "application/x-msterminal"},
            {".tsi", "audio/tsplayer"},
            {".tsp", "application/dsptype"},
            {".tsv", "text/tab-separated-values"},
            {".ttf", "application/octet-stream"},
            {".ttz", "application/t-time"},
            {".txt", "text/plain"},
            {".ult", "audio/x-mod"},
            {".ustar", "application/x-ustar"},
            {".uu", "application/x-uuencode"},
            {".uue", "application/x-uuencode"},
            {".vcd", "application/x-cdlink"},
            {".vcf", "text/x-vcard"},
            {".vdo", "video/vdo"},
            {".vib", "audio/vib"},
            {".viv", "video/vivo"},
            {".vivo", "video/vivo"},
            {".vmd", "application/vocaltec-media-desc"},
            {".vmf", "application/vocaltec-media-file"},
            {".vmi", "application/x-dreamcast-vms-info"},
            {".vms", "application/x-dreamcast-vms"},
            {".vox", "audio/voxware"},
            {".vqe", "audio/x-twinvq-plugin"},
            {".vqf", "audio/x-twinvq"},
            {".vql", "audio/x-twinvq"},
            {".vre", "x-world/x-vream"},
            {".vrml", "x-world/x-vrml"},
            {".vrt", "x-world/x-vrt"},
            {".vrw", "x-world/x-vream"},
            {".vts", "workbook/formulaone"},
            {".wax", "audio/x-ms-wax"},
            {".wbmp", "image/vnd.wap.wbmp"},
            {".web", "application/vnd.xara"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wi", "image/wavelet"},
            {".wis", "application/x-InstallShield"},
            {".wm", "video/x-ms-wm"},
            {".wmd", "application/x-ms-wmd"},
            {".wmf", "application/x-msmetafile"},
            {".wml", "text/vnd.wap.wml"},
            {".wmlc", "application/vnd.wap.wmlc"},
            {".wmls", "text/vnd.wap.wmlscript"},
            {".wmlsc", "application/vnd.wap.wmlscriptc"},
            {".wmlscript", "text/vnd.wap.wmlscript"},
            {".wmv", "video/x-ms-wmv"},
            {".wmx", "video/x-ms-wmx"},
            {".wmz", "application/x-ms-wmz"},
            {".wpng", "image/x-up-wpng"},
            {".wps", "application/vnd.ms-works"},
            {".wpt", "x-lml/x-gps"},
            {".wri", "application/x-mswrite"},
            {".wrl", "x-world/x-vrml"},
            {".wrz", "x-world/x-vrml"},
            {".ws", "text/vnd.wap.wmlscript"},
            {".wsc", "application/vnd.wap.wmlscriptc"},
            {".wv", "video/wavelet"},
            {".wvx", "video/x-ms-wvx"},
            {".wxl", "application/x-wxl"},
            {".x-gzip", "application/x-gzip"},
            {".xar", "application/vnd.xara"},
            {".xbm", "image/x-xbitmap"},
            {".xdm", "application/x-xdma"},
            {".xdma", "application/x-xdma"},
            {".xdw", "application/vnd.fujixerox.docuworks"},
            {".xht", "application/xhtml+xml"},
            {".xhtm", "application/xhtml+xml"},
            {".xhtml", "application/xhtml+xml"},
            {".xla", "application/vnd.ms-excel"},
            {".xlc", "application/vnd.ms-excel"},
            {".xll", "application/x-excel"},
            {".xlm", "application/vnd.ms-excel"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".xlt", "application/vnd.ms-excel"},
            {".xlw", "application/vnd.ms-excel"},
            {".xm", "audio/x-mod"},
            {".xml", "text/xml"},
            {".xmz", "audio/x-mod"},
            {".xpi", "application/x-xpinstall"},
            {".xpm", "image/x-xpixmap"},
            {".xsit", "text/xml"},
            {".xsl", "text/xml"},
            {".xul", "text/xul"},
            {".xwd", "image/x-xwindowdump"},
            {".xyz", "chemical/x-pdb"},
            {".yz1", "application/x-yz1"},
            {".z", "application/x-compress"},
            {".zac", "application/x-zaurus-zac"},
            {".zip", "application/zip"},
            {"", "*/*"}};


    public static void openFile(Context context, File file) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            Uri contentUri;
            if (Build.VERSION.SDK_INT >= 24) {
                contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
            } else {
                contentUri = Uri.fromFile(file);
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri); // 必须添加这一句，否则找不到资源
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(contentUri, getMIMEType(file));
            context.startActivity(Intent.createChooser(intent, "请选择对应的软件打开该附件！"));
        } catch (Exception e) {
            Toast.makeText(context, "sorry附件不能打开，请下载相关软件！", Toast.LENGTH_SHORT).show();
        }
    }


    public static boolean isFileExit(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    public static boolean cerateNewFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 删除空目录
     *
     * @param dir 将要删除的目录路径
     */
    private static void doDeleteEmptyDir(String dir) {
        boolean success = (new File(dir)).delete();
        if (success) {
            System.out.println("Successfully deleted empty directory: " + dir);
        } else {
            System.out.println("Failed to delete empty directory: " + dir);
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


}
