// Karma configuration
// Generated on Mon Jun 24 2013 11:15:20 GMT+0200 (CEST)


// base path, that will be used to resolve files and exclude
basePath = 'js';


// list of files / patterns to load in the browser
files = [
  JASMINE,
  JASMINE_ADAPTER,
  REQUIRE,
  REQUIRE_ADAPTER,
  {pattern: 'specs/basicSpec.js', included: false},
  {pattern: 'specs/deleteSpec.js', included: false},
  {pattern: 'specs/helper.js', included: false},
  'test-main.js'
];


// list of files to exclude
exclude = [
  
];

// test results reporter to use
// possible values: 'dots', 'progress', 'junit'
reporters = ['progress'];


// web server port
port = 9876;


// cli runner port
runnerPort = 9100;

proxies = {
    '/play': 'http://localhost:3333'
};


// enable / disable colors in the output (reporters and logs)
colors = true;


// level of logging
// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
logLevel = LOG_INFO;


// enable / disable watching file and executing tests whenever any file changes
autoWatch = false;


// Start these browsers, currently available:
// - Chrome
// - ChromeCanary
// - Firefox
// - Opera
// - Safari (only Mac)
// - PhantomJS
// - IE (only Windows)
browsers = ['Chrome'];


// If browser does not capture in given timeout [ms], kill it
captureTimeout = 60000;


// Continuous Integration mode
// if true, it capture browsers, run tests and exit
singleRun = true;
