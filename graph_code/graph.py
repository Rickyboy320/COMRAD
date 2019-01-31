import matplotlib.pyplot as plt
import numpy as np
import matplotlib.lines as mlines
plt.style.use('seaborn-talk')
fig, ax = plt.subplots()

# No Streaming toto
B_A = [40200, 38870, 42596, 42910, 34939]
C_A = [122905, 103725, 120998, 116991, 88775]
B_C = [141273, 147985, 116313, 114094]
C_B = []

# # No streaming over the horizon
# B_A = [28182, 47233, 26359, 43180, 28393]
# C_A = [121680, 106393, 124385, 111115, 123750]

# Streaming toto
B_A_play = [3390, 3390, 3791, 3200, 4153, 2797]
B_A_full = [50288, 59171, 60291, 53191, 47520]

C_A_play = [19834, 16826, 14365, 11189, 23427]
C_A_full = [197850, 227763, 214046, 124695, 133162]

C_B_play = [4062, 2830, 2708, 2648, 2732]
C_B_full = [48583, 47072, 44086, 43244, 42741]

B_C_play = []
B_C_full = []

bar_width = 0.35
index = np.arange(2)

opacity = 0.4
error_config = {'ecolor': 'k'}

rects1 = ax.bar(index, [np.mean(B_A), np.mean(C_A)], bar_width,
                # color='#616161',
                yerr=[np.std(B_A), np.std(C_A)], error_kw=error_config,
                label='Full transfer; no Streaming')

rects2 = ax.bar(index + bar_width, [np.mean(B_A_full), np.mean(C_A_full)], bar_width,
                # color='#303f9f',
                yerr=[np.std(B_A_full), np.std(C_A_full)], error_kw=error_config,
                label='Full transfer; Streaming')

rects3 = ax.bar(index + bar_width, [np.mean(B_A_play), np.mean(C_A_play)], bar_width,
                # color='m',
                yerr=[np.std(B_A_play), np.std(C_A_play)], error_kw=error_config,
                label='Time until playing; Streaming')

ax.set_xlabel('Amount of hops')
ax.set_ylabel('Time from request to full transfer in milliseconds')
ax.set_title('Streaming vs Non Streaming\n 6.29 MB audio file duration 4 min 34 seconds (Mean over 5 runs)')
ax.set_xticks(index + bar_width / 2)
ax.set_xticklabels(('1 hop', '2 hops', 'C', 'D', 'E'))
blue_line = mlines.Line2D([], [], color='k', marker='|',
                          markersize=15, label='Standard deviation')
ax.legend(handles=[blue_line, rects1, rects2, rects3], loc=2)

fig.tight_layout()
plt.savefig("StreamingvsNonStreaming.pdf", format="pdf")
plt.savefig("StreamingvsNonStreaming.svg", format="svg")
plt.savefig("StreamingvsNonStreaming.png", format="png")

# Stress
# A_B_play_stress = [4470, 4279, 4939, 3868]
# C_B_play_stress = [3646, 4276, 3757, 4928]
# A_B_full_stress = [120767, 119525, 133531, 104933]
# C_B_full_stress = [87003, 93329, 98629, 113541]

A_B_play = [10883, 22821, 21331, 25943, 13925]
A_B_full = [93411, 126165, 106984, 183333, 103494]

C_B_play = [5083, 4423, 4946, 5215, 4417]
C_B_full = [117140, 112550, 113445, 114465, 114997]

A_B_play_stress = [7541, 22723, 34391, 37579, 37489]
A_B_full_stress = [190122, 206827, 224451, 220842, 209547]

C_B_play_stress = [10997, 10093, 10778, 13957, 10945]
C_B_full_stress = [221311, 236097, 223014, 228022, 232910]

fig, ax = plt.subplots()
rects1 = ax.bar(index, [np.mean(A_B_full_stress), np.mean(C_B_full_stress)], bar_width,
                # color='#616161',
                yerr=[np.std(A_B_full_stress), np.std(C_B_full_stress)], error_kw=error_config,
                label='Full transfer; Concurrent streaming')

rects2 = ax.bar(index + bar_width, [np.mean(A_B_full), np.mean(C_B_full)], bar_width,
                # color='#303f9f',
                yerr=[np.std(C_B_full), np.std(C_B_full)], error_kw=error_config,
                label='Full transfer; Single stream')

rects3 = ax.bar(index, [np.mean(A_B_play_stress), np.mean(C_B_play_stress)], bar_width,
                # color='m',
                yerr=[np.std(A_B_play_stress), np.std(C_B_play_stress)], error_kw=error_config,
                label='Time until playing; Concurrent streaming')

rects4 = ax.bar(index + bar_width, [np.mean(A_B_play), np.mean(C_B_play)], bar_width,
                # color='r',
                yerr=[np.std(A_B_play), np.std(C_B_play)], error_kw=error_config,
                label='Time until playing; Single stream')

ax.set_xlabel('Concurrent streaming vs Single streaming\n6.29 MB audio file duration 4 min 34 seconds (Mean over 5 runs)')
ax.set_ylabel('Time from request to full transfer in milliseconds')
ax.set_xticks(index + bar_width / 2)
ax.set_xticklabels(('Samsung S5 mini to Samsung S7', 'Samsung S5 mini to Samsung S3 Neo', 'C', 'D', 'E'))
# ax.legend(loc=2)

blue_line = mlines.Line2D([], [], color='k', marker='|',
                          markersize=15, label='Standard deviation')
# ax.legend(, loc=2)
ax.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc='lower left',
           ncol=1, handles=[blue_line, rects1, rects2, rects3, rects4], mode="expand", borderaxespad=0.)
# ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0.)

fig.tight_layout()
plt.savefig("stressvsnonstress.pdf", format="pdf")
plt.savefig("stressvsnonstress.svg", format="svg")
plt.savefig("stressvsnonstress.png", format="png")

plt.clf()
##### Packet size
packet = {
    "128000" : {
        "play" : [6712, 10028, 9415],
        "full" : [138427, 94045, 103903]
    },
    "192000" : {
        "play" : [1935, 9621, 5938],
        "full" : [41455, 51106, 49956]
    },
    "256000" : {
        "play" : [3199, 10261, 9066],
        "full" : [42319, 57345, 55117]
    },
    "384000" : {
        "play" : [9431, 13763, 12658],
        "full" : [48842, 59005, 45649]
    },
    "512000" : {
        "play" : [30558, 21469, 25751],
        "full" : [137499, 151969, 150138]
    }
}

x = []
y_play = []
y_full = []
for key in packet:
    values = packet[key]
    x.append(int(key))
    y_play.append(np.mean(values["play"]))
    y_full.append(np.mean(values["full"]))

plt.plot(x, y_play, label="Time until playing")
plt.plot(x, y_full, label="Full stream")
plt.scatter(x, y_play)
plt.scatter(x, y_full)
plt.title("Playing and full transfer speed vs packet size\n 6.29 MB audio file duration 4 min 34 seconds(Mean over 3 runs)")
plt.xlabel('Packet size in bytes')
plt.ylabel('Time from request to playing and full transfer in milliseconds')
# ax.legend(loc=2)
# ax.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc='lower left',
#            ncol=1, mode="expand", borderaxespad=0.)

plt.legend()
# ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0.)

fig.tight_layout()
plt.savefig("Packetsizetest.pdf", format="pdf")
plt.savefig("Packetsizetest.svg", format="svg")
plt.savefig("Packetsizetest.png", format="png")