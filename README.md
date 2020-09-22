# COVID Effort (Finder) Experiment Code

## Software Requirements

**Supported OS**: Linux / macOS

**Required Softwares**:

- Bash
- Gradle: (version: 6.0+)
- Java (JDK): (version: 11+)
- Docker (version: 19+)
- Python 3.6.9 + 
- psql: PostgreSQl client program

## Data File Download
Please download the (anonymized) data from:  https://drive.google.com/file/d/1z1oUcM5JGWSg84KCgpqaR4d5Qr9c5N5E/view?usp=sharing


## Brief Intro

Finder is an effort to provide privacy-preserving COVID apps (contact tracing, location tracing, social distancing tracing..) based on device association data in a Wi-Fi network. Finder has two modules: the encrypter module reads from the Wi-Fi data CSV files, encrypt data and ingest the data into a DB and the querier module generates encrypted SQL query according to the input plain-text query. It queries DB using generated SQL query and parse the encrypted data to get the result for the query. 

Finder is implemented in Java and provides Python script as driver program for running the experiment locally. Please refer to the README.md document in each module, modify the environment and experiment parameters in the Python script (run_exp.py) and run the experiment. The execution time of experiment will be shown in the term when the program is finished.



