package ocr;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;


/**
 * a class to get the real path of a file based on Uri
 * @author: tianma
 * retrieved from: http://www.jianshu.com/p/b168cbe50066
 */

public class FilePath {

    /**
     * get absolute image path given image Uri, applicable for API 19+
     *
     * @param context
     * @param uri     image Uri
     * @return if the image exists, return absolute path of that image, otherwise return null
     *
     */
    @SuppressLint("NewApi")
    public static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // if this is document uri
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // use ':' to split
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            }
            else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())){
            // if this is content type Uri
            filePath = getDataColumn(context, uri, null, null);
        }
        else if ("file".equals(uri.getScheme())) {
            // if this uri is file type, get absolute path directly.
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * get the column _data in database，which is the file path of the Uri
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}
