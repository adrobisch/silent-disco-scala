module.exports = function(config) {
  config.set({
    // base path, that will be used to
    // resolve files and exclude

    // we use /silent-disco/client/src
    basePath: '../../../',
    frameworks: ['jasmine', 'requirejs' ],

    files: [
      { pattern: 'main/webapp/**/*.js', included: false },
      { pattern: 'test/javascript/unit/**/*.js', included: false },
      { pattern: 'test/javascript/lib/**/*.js', included: false },

      'test/javascript/config/require-unit-bootstrap.js'
    ],

    browsers: ["Chrome"], // "PhantomJS", "Firefox"

    autoWatch: true,

    junitReporter: {
      outputFile: '../../../../target/failsafe-reports/js-unit.xml',
      suite: 'unit'
    },

    plugins: [
      'karma-chrome-launcher',
      'karma-firefox-launcher',
      'karma-phantomjs-launcher',
      'karma-jasmine',
      'karma-requirejs'
    ]
  });
};