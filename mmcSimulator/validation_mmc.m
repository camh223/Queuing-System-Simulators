u = 1/0.01;
k = 100;

p = zeros(1, k);
ana_Lq = zeros(1, k);
ana_Wq = zeros(1, k);
c = double(c)

for i = 1 : length(sim_lambda)
    p(i) = sim_lambda(i) / (c * u);
    x = 0;
    for n = 0 : c-1
        x = x + ((c * p(i)) ^ n)/ factorial(n);
    end
    P0 = 1 / (x + ((c * p(i)) ^ c) * (1/factorial(c) * (1/(1-p(i)))));
    ana_Lq(i) = (((c * p(i)) ^ (c + 1)) * P0) / ((c * factorial(c)) * (1 - p(i)) ^ 2);
    ana_Wq(i) = ana_Lq(i) / i;
end

figure(1)
t2 = tiledlayout(1, 2);
title(t2, 'Results')
ana_lambda = linspace(0, 100);

nexttile
plot(sim_lambda, sim_Wq, '-');
hold on
plot(sim_lambda, ana_Wq, 'x');
xlabel('Lambda');
ylabel('Average Waiting Time');
legend("Simulation Results", "Analytical Results");
hold off

nexttile
plot(sim_lambda, sim_Lq, '-');
hold on
plot(sim_lambda, ana_Lq, 'x');
xlabel('Lambda');
ylabel('Average Queue Length');
legend("Simulation Results", "Analytical Results");
hold off