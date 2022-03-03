use std::error::Error;
use std::panic::catch_unwind;
use std::thread;
use simple_logger::SimpleLogger;
use winapi::um::consoleapi::AllocConsole;

fn open_console() {
    unsafe { AllocConsole(); }
    SimpleLogger::new().init().unwrap();
    log::info!("Opened client debug console.");
}

fn init() -> Result<(), Box<dyn Error>> {
    /*
     * We open the debugging console during development.
     * Its best if this line is commented out before creating a client
     * which will be used for distribution.
     */
    open_console();

    Ok(())
}

#[mem::dll_main]
fn main() {
    thread::spawn(move || {
        match catch_unwind(init) {
            Ok(ret) => {
                if let Some(err) = ret.err() { log::error!("Failed to bootstrap. Error: {:?}", err); }
            }
            Err(err) => { log::error!("Failed to bootstrap. Error: {:?}", err); }
        }
    });
}