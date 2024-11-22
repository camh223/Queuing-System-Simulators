figure(1)
title('Blocking Probability for M/M/C/C Simulator')
plot(sim_lambda, sim_Pc);
xlabel('Arrival Rate');
ylabel('Blocking Probability');

figure(2)
title('Server Utilisation for M/M/C/C Simulator')
plot(sim_lambda, sim_p);
xlabel('Arrival Rate');
ylabel('Server Utilisation')