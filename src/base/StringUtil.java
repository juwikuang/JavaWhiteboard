package base;

//Remove this when upgrading to Java 8
public class StringUtil {
    
    public static String join(CharSequence cs, Iterable<? extends CharSequence> itrbl)
    {
        String result="";
        for(CharSequence s : itrbl)
        {
            result+=s;
            result+=cs;
        }
        return result;
    }
}
