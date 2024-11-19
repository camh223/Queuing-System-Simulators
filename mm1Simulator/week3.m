figure(1)
plot(sim_Wq, sim_Lq, '-');
title('Simulation Results')
xlabel('Mean Waiting Time');
ylabel('Average Queue Length');

u = 1/0.01;
k = 100;

for i = 1 : k
    p(i) = i / u;
    ana_Wq(i) = (p(i) / u) / (1-p(i));
    ana_Lq(i) = (p(i)^2) / (1-p(i));
end

figure(2)
plot(ana_Wq, ana_Lq, '-');
title('Analytical Results')
xlabel('Mean Waiting Time');
ylabel('Average Queue Length');