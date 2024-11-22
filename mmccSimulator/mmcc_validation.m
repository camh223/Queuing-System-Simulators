u = double(u);
n = length(sim_lambda);

P0 = zeros(1, n);
ana_Pc = zeros(1, n);
c = double(c);

for i = 1 : length(sim_lambda)
    x = 0;
    for k = 0 : c
        x = x + (((sim_lambda(i) / u) ^ k) * (1 / factorial(k)));
    end
    P0(i) = 1 / x;
    ana_Pc(i) =  P0(i) * (((sim_lambda(i) / u) ^ c) / factorial(c));
end

figure(1)
title('Results')
plot(sim_lambda, sim_Pc, '-');
hold on
plot(sim_lambda, ana_Pc, 'x');
xlabel('Lambda');
ylabel('Blocking Probability');
legend("Simulation Results", "Analytical Results");
hold off
