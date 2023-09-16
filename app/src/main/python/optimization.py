import ast
import re
from gekko import GEKKO

def optimization(data):
    # convert data to string
    data_str = str(data)
    # Remove newlines and spaces
    data_str = data_str.replace('\n', '').replace(' ', '')
    # Extract keys and values using regex
    matches = re.findall(r'(\w+)=\[(.*?)\]', data_str)
    # Create the dictionary
    data_dict = {key: list(map(float, values.split(','))) for key, values in matches}

    # data_dict = {
    #     "obj": [3.0, 3.0, 3.0, 3.0, 3.5, 3.0, 17.0, 38.0, 23.0, 23.0, 17.0, 21.0, 20.0, 17.0, 15.0, 15.0, 60.0, 5.0],
    #     "cp": [8.0, 7.0, 8.0, 3.5, 6.0, 3.0, 8.1, 42.0, 22.0, 32.0, 12.0, 16.0, 18.0, 22.0, 20.0, 50.0, 0.0, 0.0],
    #     "tdn": [52.0, 50.0, 60.0, 40.0, 42.0, 42.0, 79.2, 70.0, 70.0, 70.0, 70.0, 72.2, 45.0, 70.0, 65.0, 77.0, 0.0, 0.0],
    #     "ca": [0.38, 0.3, 0.53, 0.18, 0.15, 0.53, 0.53, 0.36, 0.2, 0.31, 1.067, 0.3, 0.3, 0.5, 0.5, 0.29, 32.0, 0.0],
    #     "ph": [0.36, 0.25, 0.14, 0.08, 0.09, 0.14, 0.41, 1.0, 0.9, 0.72, 0.093, 0.62, 0.62, 0.45, 0.4, 0.68, 15.0, 0.0],
    #     "per": [40.0, 10.0, 40.0, 20.0, 10.0, 40.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 0.1, 0.1]
    # }
    print(data_dict)

    # Assign variables
    obj = data_dict['obj']
    cp = data_dict['cp']
    tdn = data_dict['tdn']
    ca = data_dict['ca']
    ph = data_dict['ph']
    per = data_dict['per']

    # Print the variables
    print(f"\"obj\": {obj},")
    print(f"\"cp\": {cp},")
    print(f"\"tdn\": {tdn},")
    print(f"\"ca\": {ca},")
    print(f"\"ph\": {ph},")
    print(f"\"per\": {per}")

    m = GEKKO(remote=True)  # Initialize gekko
    x = [m.Var(value=0, lb=0, ub=5) for _ in range(len(obj))]

    # Equations
    m.Minimize(sum([obj[i] * x[i] for i in range(len(obj))]))  # Objective
    m.Equation(sum(x) == 1.06)
    m.Equation((sum([cp[i] * x[i] for i in range(len(cp))]) / 100) >= 0.176)  # cp LB
    m.Equation((sum([tdn[i] * x[i] for i in range(len(tdn))]) / 100) >= 0.688)  # TDN LB
    m.Equation(((0.3 * 1.06) * (x[0] + x[1]) - (0.4 * 1.06) * (x[2] + x[3])) == 0)  # G & D
    m.Equation(sum(x[4:]) >= (0.4 * 1.06))  # Con LB
    m.Equation(sum(x[4:]) <= (0.6 * 1.11))  # Con UB
    m.Equation(x[len(obj) - 1] >= (0.01 * 1.06))
    m.Equation(x[len(obj) - 1] <= (0.02 * 1.06))
    m.Equation((sum([ca[i] * x[i] for i in range(len(ca))]) / 100) >= 0.00424)  # Ca LB
    m.Equation((sum([ca[i] * x[i] for i in range(len(ca))]) / 100) <= 0.00428)  # Ca UB
    m.Equation((sum([ph[i] * x[i] for i in range(len(ph))]) / 100) >= 0.004134)  # P LB
    m.Equation((sum([ph[i] * x[i] for i in range(len(ph))]) / 100) <= 0.00418)  # P UB

    m.options.IMODE = 3  # Steady state optimization
    m.solve()  # Solve

    results = [x[i].value[0] for i in range(len(obj))]


    result = ""
    for i, r in enumerate(results):
        result += f"x{i + 1}: {r}<br>"
        print(f"x{i + 1}: {r}")

    # print(results)
    return results
#     return f"<h1>Sample data for optimization API testing:</h1><br><h3>{data}</h3><br><h2>Output:</h2><br><h3>{result}</h3><br><h3>Time taken: xxx seconds</h3>"

if __name__ == "__main__":
    data_dict = {
        "obj": [3.0, 3.0, 3.0, 3.0, 3.5, 3.0, 17.0, 38.0, 23.0, 23.0, 17.0, 21.0, 20.0, 17.0, 15.0, 15.0, 60.0, 5.0],
        "cp": [8.0, 7.0, 8.0, 3.5, 6.0, 3.0, 8.1, 42.0, 22.0, 32.0, 12.0, 16.0, 18.0, 22.0, 20.0, 50.0, 0.0, 0.0],
        "tdn": [52.0, 50.0, 60.0, 40.0, 42.0, 42.0, 79.2, 70.0, 70.0, 70.0, 70.0, 72.2, 45.0, 70.0, 65.0, 77.0, 0.0, 0.0],
        "ca": [0.38, 0.3, 0.53, 0.18, 0.15, 0.53, 0.53, 0.36, 0.2, 0.31, 1.067, 0.3, 0.3, 0.5, 0.5, 0.29, 32.0, 0.0],
        "ph": [0.36, 0.25, 0.14, 0.08, 0.09, 0.14, 0.41, 1.0, 0.9, 0.72, 0.093, 0.62, 0.62, 0.45, 0.4, 0.68, 15.0, 0.0],
        "per": [40.0, 10.0, 40.0, 20.0, 10.0, 40.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 0.1, 0.1]
    }
    optimization(data_dict)