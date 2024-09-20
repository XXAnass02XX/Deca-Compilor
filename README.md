### Project Overview

This compiler is built **from scratch** for the Deca language, a subset of Java.  

We've added an extension to generate assembly code for the Game Boy.

Our test scripts for each stage of the compiler, as well as the extension, can be found in the `src/test/script/` directory:  

- **tests_lexico.sh**: Runs the lexicon tests.  
- **tests_syntaxe.sh**: Runs the syntax tests.  
- **tests_context_invalides.sh**: Runs invalid context tests, listing all contextual rules.  
- **tests_codegen.sh**: Runs tests for the code generation step (assembly code).  
- **tests_gameboy.sh**: Runs tests for the Game Boy extension.

### Game Boy Tests Explanation:

For the Game Boy tests, we've commented out the emulator launch. The script only compiles the files and stops.  
To run tests with the emulator, you need to install **RGBDS** and **Emulicious**.

After installation, uncomment the following lines in the `doVerify()` function inside `tests_gameboy.py`:

- `os.system(f"rgbasm -L -o {decaFileNameNoExt}.o {decaFileNameNoExt}.asm")`  
- `os.system(f"rgblink -o {decaFileNameNoExt}.gb {decaFileNameNoExt}.o")`  
- `os.system(f"rgbfix -v -p 0xFF {decaFileNameNoExt}.gb")`  
- `os.system(f"Emulicious.jar {decaFileNameNoExt}.gb &")`  

Additionally, to prevent the emulator from closing immediately, add:  
`input("Enter to Exit...")`

At the top of the file, modify the `maxTest` variable to avoid opening too many emulators at once. A value of 8 is recommended.

For each window, if "Nintendo" is displayed, the test is successful. If "Hello World" is displayed, the test has failed.