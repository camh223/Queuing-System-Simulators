import java.util.Arrays;
import com.mathworks.engine.*;
import java.util.Random;

public class m1m2mccSimulator {

    // System State Variables
    private int[] server_status;

    // Global Variables
    private double sim_time;
    private double time_last_event;
    public int next_event_type;
    private double[] time_next_event;
    private double[] area_server_status;
    private double lambda1;
    private double lambda2;
    private double mean_interarrival;
    private double mean_handover;
    private double mean_service;
    private double u;
    private int c; // No of servers
    private int n; // Threshold
    private int current_threshold;
    private int servers_free;
    public int num_custs_required;
    public int num_customer;
    public int num_handover;
    public long seed;
    private double time_past;
    private double[] server_utilisation;
    private int num_events;
    private double min_time_next_event;
    private int server_idle;
    private double total_server_utilisation;
    private double total_arrival_loss;
    private double total_handover_loss;
    private double call_blocking_probability;
    private double handover_failure_probability;
    private double aggregated_blocking_probability;
    private Random random;

    public m1m2mccSimulator(double lambda1, double lambda2, double u, int num_custs_required, int c, int n, long seed) {
        this.num_custs_required = num_custs_required;
        this.lambda1 = lambda1;
        this.lambda2 = lambda2;
        this.u = u;
        this.c = c;
        this.n = n;
        this.time_next_event = new double[c+2];
        this.server_status = new int[c+1];
        this.area_server_status = new double[c+1];
        this.server_utilisation = new double[c+1];
        this.random = new Random(seed);
    }

    public void initialise() {
        // System.out.println("Initialise");
        mean_interarrival = 1/lambda1;
        mean_handover = 1/lambda2;
        mean_service = 1/u;
        sim_time = 0.0;
        // initialise state variables
        num_customer = 0;
        num_handover = 0;
        num_events = c+2;
        for (int i = 1; i <= c; i++) {
            server_status[i] = 0; // IDLE
            area_server_status[i] = 0;
        }
        // initialise statistical counters
        time_last_event = 0.0;
        total_arrival_loss = 0;
        total_handover_loss = 0;
        // initialise event list
        time_next_event[0] = sim_time + expon(mean_interarrival);
        time_next_event[c+1] = sim_time + expon(mean_handover);
        for (int i = 1; i <= c; i++) {
            time_next_event[i] = Double.MAX_VALUE;
        }
        // initialise random number generator
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

    public void arrive(int j) {
        // System.out.println("Arrival");
        // schedule next arrival
        // Find out idle server
        server_idle = 0;
        int i = 1;
        servers_free = 0;
        if (j == 0) {
            time_next_event[j] = sim_time + expon(mean_interarrival);
            current_threshold = n;
            num_customer++;
        } else {
            time_next_event[j] = sim_time + expon(mean_handover);
            current_threshold = 0;
            num_handover++;
        }
        while (i <= c) {
            // System.out.println("i: " + i);
            if (server_status[i] == 0) {
                if (server_idle == 0) {
                    server_idle = i;
                }
                servers_free++;
            }
            i++;
        }
        if (servers_free > current_threshold) { // Someone is IDLE
            server_status[server_idle] = 1;
            time_next_event[server_idle] = sim_time + expon(mean_service);
        } else if (j == 0) { // server is BUSY
            total_arrival_loss++;
        } else {
            total_handover_loss++;
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
        System.out.println("Total_arrival_loss: " + total_arrival_loss);
        System.out.println("Num_customer: " + num_customer);
        System.out.println("total_handover_loss: " + total_handover_loss);
        System.out.println("num_handover: " + num_handover);
        call_blocking_probability = total_arrival_loss / num_customer;
        System.out.println("Call Blocking Probability: " + call_blocking_probability);
        handover_failure_probability = total_handover_loss / num_handover;
        System.out.println("Handover failure probability: " + handover_failure_probability);
        aggregated_blocking_probability = call_blocking_probability + (10 * handover_failure_probability);
        for (int i = 1; i <= c; i++) {
            server_utilisation[i] = area_server_status[i]/sim_time;
            total_server_utilisation += area_server_status[i];
        }
        total_server_utilisation = total_server_utilisation/sim_time/c;
    }

    public double expon(double mean) {
        double x = random.nextDouble();
        return (-mean * Math.log(x));
    }

    public void main_sim() {
        initialise();

        while (num_customer < num_custs_required) {
            timing();
            update_time_avg_stats();
            if (next_event_type == 0 || next_event_type == c + 1) {
                arrive(next_event_type);
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
        m1m2mccSimulator sim;
        double[] arrival_rates = new double[101];
        double[] total_server_utils = new double[101];
        double[] call_blocking_probabilities = new double[101]; 
        double[] handover_failure_probabilities = new double[101];
        double[] aggregated_blocking_probabilities = new double[101];
        double u = 1.0/100.0;
        int c = 16; // Number of Servers
        int n = 2; // Threshold
        double handover_rate = 0.1;
        double arrival_rate;
        double cumul_server_util;
        double cumul_CBP;
        double cumul_HFP;
        double cumul_ABP;

        for (int i = 1; i < 101; i++) {
            System.out.println("Current Run: "+ i);
            arrival_rate = i/100.0;
            cumul_server_util = 0.0;
            cumul_CBP = 0.0;
            cumul_HFP = 0.0;
            cumul_ABP = 0.0;
            for (int j = 1; j <= 5; j++) {
                sim = new m1m2mccSimulator(arrival_rate, handover_rate, u, 15000, c, n, j);
                sim.main_sim();
                cumul_server_util += sim.total_server_utilisation;
                cumul_CBP += sim.call_blocking_probability;
                cumul_HFP += sim.handover_failure_probability;
                cumul_ABP += sim.aggregated_blocking_probability;
            }
            arrival_rates[i] = arrival_rate;
            total_server_utils[i] = cumul_server_util / 5.0;
            call_blocking_probabilities[i] = cumul_CBP / 5.0;
            handover_failure_probabilities[i] = cumul_HFP / 5.0;
            aggregated_blocking_probabilities[i] = cumul_ABP / 5.0;
        }
        eng.putVariable("sim_lambda2", arrival_rates);
        eng.putVariable("sim_lambda1", handover_rate);
        eng.putVariable("u", u);
        eng.putVariable("c", c);
        eng.putVariable("n", n);
        eng.putVariable("sim_CBP", call_blocking_probabilities);
        eng.putVariable("sim_HFP", handover_failure_probabilities);
        System.out.println("Total Server Utilisation: "+ Arrays.toString(total_server_utils));
        System.out.println("Call Blocking Probability: " + Arrays.toString(call_blocking_probabilities));
        System.out.println("Handoff Failure Probabilities: " + Arrays.toString(handover_failure_probabilities));
        eng.close();
    }
}