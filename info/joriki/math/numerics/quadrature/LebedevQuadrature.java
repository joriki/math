package info.joriki.math.numerics.quadrature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

// The points and weights are from https://people.sc.fsu.edu/~jburkardt/datasets/sphere_lebedev_rule/sphere_lebedev_rule.html
public class LebedevQuadrature {
    public double [] theta;
    public double [] phi;
    public double [] weights;
    
    public LebedevQuadrature (int precision) throws IOException {
        for (;;precision++) {
            if (precision > 131)
                throw new IllegalArgumentException ();
            InputStream in = LebedevQuadrature.class.getResourceAsStream (String.format ("/info/joriki/math/numerics/quadrature/lebedev/lebedev_%03d.txt",precision));
            if (in != null) {
                BufferedReader reader = new BufferedReader (new InputStreamReader (in));
                List<String> lines = new ArrayList<>();
                for (;;) {
                    String line = reader.readLine ();
                    if (line == null)
                        break;
                    lines.add (line);
                }
                reader.close ();
                theta = new double [lines.size ()];
                phi = new double [lines.size ()];
                weights = new double [lines.size ()];
                int i = 0;
                for (String line : lines) {
                    String [] values = line.trim ().split ("\\s+");
                    if (values.length != 3)
                        throw new Error ();
                    phi [i] = (Math.PI / 180) * Double.parseDouble (values [0]);
                    theta [i] = (Math.PI / 180) * Double.parseDouble (values [1]);
                    weights [i] = Double.parseDouble (values [2]);
                    i++;
                }
                return;
            }
        }
    }
    
    public double integrate (DoubleBinaryOperator f) {
        double sum = 0;
        for (int i = 0;i < weights.length;i++)
            sum += weights [i] * f.applyAsDouble (theta [i],phi [i]);
        return sum;
    }
}
