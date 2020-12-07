# Hong Kong Public Library

This project has some utilities to automate tasks with the Hong Kong Public Library:
https://www.hkpl.gov.hk/

Author: Kevin Connor ARPE <kevinarpe@gmail.com>

## Thanks

A special thank you is owed to Kenan Klisura for his Java library "chrome-devtools-java-client"
that makes this project possible.

Read more about it here: https://github.com/kklisura/chrome-devtools-java-client

## Build

1. Install Java JDK 11
1. Install Apache Maven 3.6+
1. Validate your Apache Maven is setup correctly: `mvn -version`
1. Checkout code: `git clone git@github.com:kevinarpe/hong-kong-public-library.git`
1. Change directory to checkout area: `cd hong-kong-public-library`
1. Build: `mvn verify`
   * There is no need to run `mvn install`
1. Result: `target/hong-kong-public-library-1.0.0-SNAPSHOT.jar`

## Auto-Renewal

Currently, the HKPL will send email notification three days in advance.
Unfortunately, there is no option for auto-renewal.  This project can do it.

There are two ready-made Bash shell scripts.  You will only need to update these scripts
to add your credentials.

* `scripts/run_renew.bash` - Run with a visible Google Chrome session
* `scripts/crontab/run_renew.bash` - Run with a invisible (headless) Google Chrome session

Both scripts...
* assume the Java 11 virtual machine (`java`) is available in your path
  * Check it with: `java -version`
* write log files to project sub-directory `log/`

If renewal is necessary, renewal is attempted, then an email will be sent.
However, if too early or no booked checked out, the script will exit without error and not send email.
Finally, if all renewals have been exhausted, an alert email is sent reminding you to return your book!

### Auto-Renewal: Crontab

Sample crontab entry to run after each reboot:
* `@reboot /home/username/git/hong-kong-public-library/scripts/crontab/run_renew.bash`
