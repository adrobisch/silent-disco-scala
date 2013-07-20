var tests = Object.keys(window.__karma__.files).filter(function (file) {
    return /Spec\.js$/.test(file);
});

requirejs.config({
    // Karma serves files from '/base'
    baseUrl: '',

    paths: {
        'ngDefine' : '/play/sd/assets/vendor/requirejs-angular-define/ngDefine',
        'jquery' :   '/play/sd/assets/vendor/jquery/jquery-1.9.1.min',
        'angular' :  '/play/sd/assets/vendor/angular/angular',
        'angular-mocks' :  '/play/sd/assets/vendor/angular/angular-mocks'
    },

    shim: {
        'angular' : { deps: [ 'jquery' ], exports: 'angular' },
        'angular-mocks' : { deps: [ 'angular' ] }
    },

    packages : [
        { name: 'disco', location: '/play/sd/app' }
    ]

    // ask Require.js to load these files (all our tests)
    //deps: tests,

    // start test run, once Require.js is done
    //callback: window.__karma__.start
});

require([ 'angular', 'angular-mocks', 'ngDefine', 'jquery'], function(angular) {
    require(["disco/pages/room"], function () {
        angular.bootstrap(document, [ "disco.pages" ]);

        require(tests, function() {
            window.__karma__.start();
        });
    });
});