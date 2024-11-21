u = double(u);
sim_lambda1 = double(sim_lambda1);
a = length(sim_lambda2);

P0 = zeros(1, a);
ana_CBP = zeros(1, a);
ana_HFP = zeros(1, a);
c = double(c);
n = double(n);

for i = 1 : length(sim_lambda2)
    x = 0;
    y = 0;
    for k = 0 : c - n
        x = x + (1 / factorial(k)) * (((sim_lambda1 + i) / u) ^ k);
    end
    for k = (c - n) + 1 : c
        x = x + (1 / factorial(k)) * (((sim_lambda1 + i) / u) ^ (c - n)) * ((sim_lambda1 / u) ^ (k - (c - n)));
    end
    P0(i) = 1 / x;
    y = y + (1 / factorial(k)) * (((sim_lambda1 + i) / u) ^ k) * P0(i);
    for k = (c - n) + 1 : c
        y = y + (1 / factorial(k)) * (((sim_lambda1 + i) / u) ^ (c - n)) * ((sim_lambda1 / u) ^ (k - (c - n))) * P0(i);
    end
    ana_CBP(i) = y;
    ana_HFP(i) = (1 / factorial(c)) * (((sim_lambda1 + i) / u) ^ (c - n)) * ((sim_lambda1 / u) ^ n) * P0(i);
end

figure(1)
t2 = tiledlayout(1, 2);
title(t2, 'Results')

nexttile
plot(sim_lambda2, sim_CBP, '-');
hold on
plot(sim_lambda2, ana_CBP, 'x');
xlabel('New Arrival Rate');
ylabel('Call Blocking Probability');
legend("Simulation Results", "Analytical Results");
hold off

nexttile
plot(sim_lambda2, sim_HFP, '-');
hold on
plot(sim_lambda2, ana_HFP, 'x');
xlabel('New Arrival Rate');
xlabel('Handoff Failure Probability');
hold off
