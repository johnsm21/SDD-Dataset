# SDD-Dataset
The SDD-Dataset [1] is composed of complex tables, data dictionaries, and semantic data dictionaries from a variety of sources, including the National Institute of Environmental Health Sciences’ (NIEHS) Human Health Exposure Analysis Resource (HHEAR), the National Center for Health Statistics’ (HCHS) National Health and Nutrition Examination Survey (NHANES), and the National Cancer Institute’s (NCI) The Cancer Genome Atlas (TCGA). All tables and data dictionaries had been manually aligned by domain experts using semantic data dictionaries. To ensure the data quality of the SDD-Dataset, we developed a set of [tools](https://github.com/johnsm21/SDD-Validation) to standardize and validate both data dictionaries and semantic data dictionaries.

## Study Manifest
Each group of studies has a `study-manifest.xlsx`, which lists all the files associated with each study, labels the file type (Data, DD, SDD), and includes any DOIs. Studies have two combinations of files: [X Data files, X DD files, X SDD files]- one set of files linked to each other and [1 Data file, X DD files, X SDD files]- one table with several linked DDs and SDDs.

## HHEAR Studies
The fully cleaned datasets for NHANES and TCGA are included; however, HHEAR data can not be published outside of the data repository. To access the original files, users can register for the HHEAR data repository [here](https://hheardatacenter.mssm.edu/Account/Login). Once signed up, anyone with DOIs in the `HHEAR-Studies/2023-06-16/study-manifest.xlsx` file can access the raw data. Users can then use validation [tools](https://github.com/johnsm21/SDD-Validation) to clean this data.

## Ontologies
The `Ontologies` folder contains a copy of each ontology against which the dataset is aligned. The column-type alignment problem that the paper [1] used to compare algorithms only uses a subset of these.


## References
[1] M. Johnson, J. A. Stingone, S. Bengoa, J. Masters, and D. L. McGuinness, “Complex semantic tabular interpretation using sdd-gen,” in *18th Int. Conf. on Semantic Comput. (ICSC)*. 2024, pp. 317-322.
