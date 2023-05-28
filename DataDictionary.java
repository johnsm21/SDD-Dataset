public class DataDictionary {
   private static String _ddPath = "Hello World";
   public DataDictionary(String path){
      _ddPath = path;
   }

   public static String getDDPath(){
      return _ddPath;
   }
}
