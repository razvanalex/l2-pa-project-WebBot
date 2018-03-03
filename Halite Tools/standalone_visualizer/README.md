### Offline Visualiser

### Prerequisites (added by Smadu Razvan)

Install npm, nodejs and ruby to build this app 


Install ruby (Please install 2.5.0, not 2.3.1 as is specified there):
https://stackoverflow.com/questions/37720892/you-dont-have-write-permissions-for-the-var-lib-gems-2-3-0-directory

#### Building the electron app

1. Install node packages for the website
    ```
    cd ../../website
    npm install
    ```

2. Install node packages for libhaliteviz
    ```
    cd ../libhaliteviz
    npm install
    ```

3. Install node packages for visualizer
    ```
    cd ../tools/standalone_visualizer
    npm install
    ```

4. Build the CSS

    ```
    cd ../../website
    bundle exec jekyll build
    ```

5. Build the visualizer assets
   ```
   cd ../tools/standalone_visualizer
   npm run build
   ```

6. Start the app
    ```
    npm run start
    ```

#### Packaging

```
yarn electron-builder .
```
