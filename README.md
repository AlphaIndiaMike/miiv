# Miiv

## Description
Miiv is a utility for file organization: removes duplicates, sorts files into predefined folders, and manages hidden/system files.

## Usage
Miiv operates with the following commands:

- `miiv init {path}`: Initializes the workspace with a predefined file structure into the provided location.
- `miiv set {path}`: Defines the source folder `{path}` for which the files will be either copied or moved into the workspace. 
- `miiv copy`: Copies the files from the source folder into the workspace. Requires the `init` and `set` calls prior execution.
- `miiv move`: Moves the files from the source folder into the workspace. Duplicates are specifically addressed during this operation. Requires the `init` and `set` calls prior execution.
