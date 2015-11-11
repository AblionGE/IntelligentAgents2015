x = [2 4 6 8 10 15 20 25 30 35]

y1 = [106 277 472 904 1765 8639 18741 132929 281984 300000]
y2 = [240 290 520 440 624 2070 4228 7657 19586 54289]
y3 = [240 240 293 719 1843 2811 3145 4312 9918 33565]
y4 = [227 270 488 877 1469 3222 3965 7385 10718 46258]

figure
plot(x,y1,x,y2,x,y3,x,y4)
legend('1 vehicle', '2 vehicles', '3 vehicles', '4 vehicles', 'Location', 'northwest')
title('Execution time for centralized PDP')
xlabel('Nb of tasks')
ylabel('Execution time [ms]')

x2 = [16320 13661.5 17420.5 13062 13713.5];
meanx2 = mean(x2)

x3 = [11732.5 16932.5 15901.5 12045.0 11782.0]
meanx3 = mean(x3)

x4 = [16500.5 14223.5 17561.499999999996 20548.0 18664.5]
meanx4 = mean(x4)