const path = require("path");
const url = require("url");

const electron = require("electron");

// Keep global reference to Electron window, because it'll be closed when
// garbage collected
let mainWindow;

function createWindow() {
    mainWindow = new electron.BrowserWindow({
        width: 1280,
        height: 960,
        autoHideMenuBar: true,
        show: false,
        icon: path.join(__dirname, 'assets/icons/png/64x64.png')
    });

    const passedFile = process.argv.find(arg => arg.endsWith('.hlt'));
    const replayFile = passedFile ? path.join(__dirname, passedFile) : null;

    mainWindow.loadURL(url.format({
        pathname: path.join(__dirname, "index.html"),
        protocol: "file:",
        slashes: true,
        hash: replayFile,
    }));

    mainWindow.on("closed", function() {
        // Close the window by letting JS GC it
        mainWindow = null;
    });

    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
    });
}

electron.app.on("ready", createWindow);
electron.app.on("window-all-closed", function() {
    if (process.platform !== "darwin") {
        electron.app.quit();
    }
});
electron.app.on("activate", function() {
    if (mainWindow === null) {
        createWindow();
    }
});
