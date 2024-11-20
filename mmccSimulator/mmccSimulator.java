import java.util.Arrays;
import com.mathworks.engine.*;

public class mmccSimulator {

    // System State Variables
    private int[] server_status;

    // Global Variables
    private double sim_time;
    private double time_last_event;
    public int next_event_type;
    private double[] time_next_event;
    private double[] area_server_status;
    private double lambda;
    private double mean_interarrival;
    private double mean_service;
    private double u;
    private int c;
    public int num_custs_required;
    public int num_customer;
    private double time_past;
    private double[] server_utilisation;
    private int num_events;
    private double min_time_next_event;
    private int server_idle;
    private double total_server_utilisation;
    private double total_loss;
    private double blocking_probability;

    public mmccSimulator(double lambda, double u, int num_custs_required, int c) {
        this.num_custs_required = num_custs_required;
        this.lambda = lambda;
        this.u = u;
        this.c = c;
        this.time_next_event = new double[c+1];
        this.server_status = new int[c+1];
        this.area_server_status = new double[c+1];
        this.server_utilisation = new double[c+1];
    }

    public void initialise() {
        // System.out.println("Initialise");
        mean_interarrival = 1/lambda;
        mean_service = 1/u;
        sim_time = 0.0;
        // initialise state variables
        num_customer = 0;
        num_events = c+1;
        for (int i = 1; i <= c; i++) {
            server_status[i] = 0; // IDLE
            area_server_status[i] = 0;
        }
        // initialise statistical counters
        time_last_event = 0.0;
        total_loss = 0;
        // initialise event list
        time_next_event[0] = sim_time + expon(mean_interarrival);
        for (int i = 1; i <= c; i++) {
            time_next_event[i] = Double.MAX_VALUE;
        }
    }

    public void timing() {
        // System.out.println("Timing");
        min_time_next_event = Double.MAX_VALUE;
        // determine event type of next event to occur
        for (int i = 0; i < num_events; i++) {
            if (time_next_event[i] <= min_time_next_event) {
                min_time_next_event = time_next_event[i];
                next_event_type = i;
            }
        }

        time_last_event = sim_time;

        // advance simulation clock
        sim_time = time_next_event[next_event_type];
    }

    public void arrive() {
        // System.out.println("Arrival");
        // schedule next arrival
        time_next_event[0] = sim_time + expon(mean_interarrival);
        // Find out idle server
        int i = 1;
        server_idle = 0;
        while (server_idle == 0 && i <= c) {
            // System.out.println("i: " + i);
            if (server_status[i] == 0) {
                server_idle = i;
            }
            i++;
        }
        num_customer++;
        if (server_idle != 0) { // Someone is IDLE
            server_status[server_idle] = 1;
            time_next_event[server_idle] = sim_time + expon(mean_service);
        } else { // server is BUSY
            total_loss++;
        }
    }

    public void depart(int j) { 
        // System.out.println("Departure from server: " + j);
        server_status[j] = 0;
        time_next_event[j] = Double.MAX_VALUE;
    }

    public void update_time_avg_stats() {
        // System.out.println("Update Stats");
        // Update area accumulators for time-average statistics
        time_past = sim_time-time_last_event;
        for (int i = 1; i <= c; i++) {
            area_server_status[i] += time_past*server_status[i];
        }
    }

    public void report() {
        // System.out.println("Report Stats");
        // Compute the desired measures of performance
        blocking_probability = total_loss / num_customer;
        for (int i = 1; i <= c; i++) {
            server_utilisation[i] = area_server_status[i]/sim_time;
            total_server_utilisation += area_server_status[i];
        }
        total_server_utilisation = total_server_utilisation/sim_time/c;
    }

    public double expon(double mean) {
        double x = Math.random();
        return (-mean * Math.log(x));
    }

    public void main_sim() {
        initialise();

        while (num_customer < num_custs_required) {
            timing();
            update_time_avg_stats();
            if (next_event_type == 0) {
                arrive();
                // System.out.println("Arrival Finished");
            } else {
                depart(next_event_type);
            }
        }
        report();
    }

    public static void main(String[] args) throws Exception {
        String[] engines = MatlabEngine.findMatlab();
        MatlabEngine eng = MatlabEngine.connectMatlab(engines[0]);
        mmccSimulator sim;
        double[] arrival_rates = new double[101];
        double[] total_server_utils = new double[101];
        double[] blocking_probabilities = new double[101]; 
        int c = 3; // Number of Servers

        for (int i = 1; i < 101; i++) {
            System.out.println("Current Run: "+ i);
            sim = new mmccSimulator(i, 1/0.01, 5000, c);
            sim.main_sim();
            arrival_rates[i] = i;
            total_server_utils[i] = sim.total_server_utilisation;
            blocking_probabilities[i] = sim.blocking_probability;
        }
        eng.putVariable("sim_lambda", arrival_rates);
        eng.putVariable("c", c);
        eng.putVariable("sim_Pc", blocking_probabilities);
        System.out.println("Total Server Utilisation: "+ Arrays.toString(total_server_utils));
        System.out.println("Blocking Probability: " + Arrays.toString(blocking_probabilities));
        eng.close();
    }
}