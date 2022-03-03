use std::thread;
use simple_logger::SimpleLogger;
use winapi::shared::minwindef::{DWORD, HMODULE, LPVOID};
use winapi::um::consoleapi::AllocConsole;

fn open_console() {
    unsafe { AllocConsole(); }
    SimpleLogger::new().init().unwrap();
    log::info!("Opened client debug console.");
}

fn bootstrap() {
    /*
     * We open the debugging console during development.
     * Its best if this line is commented out before creating a client
     * which will be used for distribution.
     */
    println!("Loool");
    open_console();

    println!("Does this work!");
}

#[no_mangle]
pub extern "system" fn DllMain(_h_module: HMODULE, dw_reason: DWORD, _lp_reserved: LPVOID) -> bool {
    match dw_reason {
        1u32 => {
            thread::spawn(|| bootstrap());
        },
        _ => return false,
    };
    true
}