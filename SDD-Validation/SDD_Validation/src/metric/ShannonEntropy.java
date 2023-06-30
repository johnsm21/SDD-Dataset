package metric;

import java.util.ArrayList;

/**
 * Imported from https://gist.github.com/Snailic/1348aef1811447bbd01b11b20c0e4e8b
 *
 */
public class ShannonEntropy {
    public double entropy = 0;
    private String input;
    public ShannonEntropy(String in){
        this.input = in;
        ArrayList<charFreq> freqs = new ArrayList<>();
        String split[] = in.split("");
        for(String s : split){
            boolean flag = true;
            for(charFreq cf : freqs){
                if(cf.s.equals(s)){
                    flag = false;
                    cf.count++;
                    break;
                }
                flag = true;
            }
            if(flag){ freqs.add(new charFreq(s)); }
        }
        
        for(charFreq cf : freqs){
            int freq = cf.count;
            if(freq == 0){continue;}
            
            double c = (double)freq / in.length();
            entropy -= log2(Math.pow(c, c));
        }
    }
    
    private final double log2(double x){ return Math.log(x) / Math.log(2); }
    @Override
    public String toString(){ return "Entropy of " + this.input + ": " + this.entropy; }
    
    private class charFreq{
        public final String s;
        public int count = 1;
        public charFreq(String in){ this.s = in; }
    }
}
