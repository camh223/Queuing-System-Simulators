import java.util.Arrays;
import com.mathworks.engine.*;

public class mm1Simulator {

    static int Q_LIMIT = 20000;

    // System State Variables
    private int server_status;
    private int num_in_q;
    private double[] time_arrival;

    // Global Variables
    private double sim_time;
    private double time_last_event;
    public int next_event_type;
    private double[] time_next_event;
    private double total_of_delays;
    private double area_num_in_q;
    private double area_server_status;
    private double lambda;
    private double mean_interarrival;
    private double mean_service;
    private double u;
    public int num_custs_required;
    public int num_customer;
    private int num_custs_delayed;
    private double time_past;
    private double average_delay;
    private double average_number_in_queue;
    private double server_utilisation;

    public mm1Simulator(double lambda, double u, int num_custs_required) {
        this.num_custs_required = num_custs_required;
        this.lambda = lambda;
        this.u = u;
        this.time_arrival = new double[Q_LIMIT+1];
        this.time_next_event = new double[3];
    }

    public void initialise() {
        mean_interarrival = 1/lambda;
        mean_service = 1/u;
        sim_time = 0.0;
        // initialise state variables
        num_customer = 0;
        num_in_q = 0;
        server_status = 0; // server status is idle
        // initialise statistical counters
        area_num_in_q = 0.0;
        area_server_status = 0.0;
        time_last_event = 0.0;
        // initialise event list
        time_next_event[1] = sim_time + expon(mean_interarrival);
        time_next_event[2] = Double.MAX_VALUE;
    }

    public void timing() {
        // determine event type of next event to occur
        if (time_next_event[1] <= time_next_event[2]) {
            next_event_type = 1;
        } else {
            next_event_type = 2;
        }

        time_last_event = sim_time;

        // advance simulation clock
        sim_time = time_next_event[next_event_type];
    }

    public void arrive() {
        // schedule next arrival
        time_next_event[1] = sim_time + expon(mean_interarrival);
        
        if (server_status == 0) {
            server_status = 1;
            time_next_event[2] = sim_time + expon(mean_service);
            num_customer++;
        } else { // server is busy
            num_in_q++;
            time_arrival[num_in_q] = sim_time;
        }
    }

    public void depart() {
        if (num_in_q == 0) {
            server_status = 0;
            time_next_event[2] = Double.MAX_VALUE;
        } else {
            num_in_q--;
            total_of_delays += (sim_time - time_arrival[1]);
            num_custs_delayed++;
            num_customer++;
            time_next_event[2] = sim_time + expon(mean_service);

            for (int i = 1; i<=num_in_q; i++) {
                time_arrival[i] = time_arrival[i+1];
            }
        }
    }

    public void update_time_avg_stats() {
        // Update area accumulators for time-average statistics
        time_past = sim_time-time_last_event;
        area_num_in_q += time_past*num_in_q;
        area_server_status += time_past*server_status;
    }

    public void report() {
        // Compute the desired measures of performance
        average_delay = total_of_delays / num_customer;
        average_number_in_queue = area_num_in_q / sim_time;
        server_utilisation = area_server_status / sim_time;
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
            if (next_event_type == 1) {
                arrive();
            } else {
                depart();
            }
        }
        report();
    }

    public static void main(String[] args) throws Exception {
        String[] engines = MatlabEngine.findMatlab();
        MatlabEngine eng = MatlabEngine.connectMatlab(engines[0]);
        mm1Simulator sim;
        double[] arrival_rates = new double[101];
        double[] average_delays = new double[101];
        double[] avg_num_in_Qs = new double[101];
        double[] server_utils = new double[101];

        /* sim = new Simulator(1, 1/0.01, 500);
        sim.main_sim();
        System.out.println("Average Delay: " + sim.average_delay);
        System.out.println("Average Num in Q: " + sim.average_number_in_queue);
        System.out.println("Server Utilisation: " + sim.server_utilisation); */


        for (int i = 1; i < 101; i++) {
            System.out.println("Current Run: "+ i);
            sim = new mm1Simulator(i, 1/0.01, 10000);
            sim.main_sim();
            arrival_rates[i] = i;
            average_delays[i] = sim.average_delay;
            avg_num_in_Qs[i] = sim.average_number_in_queue;
            server_utils[i] = sim.server_utilisation;
        }
        eng.putVariable("sim_lambda", arrival_rates);
        eng.putVariable("sim_Wq", average_delays);
        eng.putVariable("sim_Lq", avg_num_in_Qs);
        System.out.println("Average Delay: "+ Arrays.toString(average_delays));
        System.out.println("Average Number in Queue: "+ Arrays.toString(avg_num_in_Qs));
        System.out.println("Server Utilisation: "+ Arrays.toString(server_utils));
        eng.close();
    }
}
