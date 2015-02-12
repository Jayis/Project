
import java.io.*;
import java.text.DecimalFormat;

import com.arc.*;


public class GetFeature{
	public static void main(String[] args) throws Exception{
		FeatureVectorExtractor fve;
        fve=new SimpleExtractor();
        String filename=args[0];
        System.out.println(filename);
        
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int lines=0,pos = 0;
        while (reader.readLine() != null) lines++;
        reader.close();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        BufferedWriter bw = new BufferedWriter(new FileWriter("feature.out",true));
        String line = null,data=null;
        while((line = br.readLine())!=null){
            pos++;
            System.out.println("processing line: "+pos+"/"+lines);
            data=doLine(line,fve);
            bw.write(data);
            bw.newLine();
        }
        br.close();
        bw.close();
	}
    private static String doLine(String line,FeatureVectorExtractor fve) throws Exception{
        String[] ops=line.split(":");
        String string=ops[0]+" ";
        String filename=ops[1];

        fve.calculate(filename);
        double[] point = fve.getFeature();
        DecimalFormat df = new DecimalFormat("#0.0000");
        for (int i = 0; i < point.length; i++) {
            string += ((i+1)+":"+df.format(point[i])+" ");
        }
        return string;
    }
}
