'use strict';
const path = require('path');
const gulp = require('gulp');
const debug = require('gulp-debug');
//const npmDist = require('gulp-npm-dist');
//onst node_modules = require('node_modules-path'); //error line 11
const node_modules = require('find-node-modules')();

console.log('node module path for this project:', node_modules);
console.log(["swagger-ui.css", "favicon-32x32.png", "favicon-16x16.png", "swagger-ui-bundle.js", "swagger-ui-standalone-preset.js"].map(i => path.resolve('.', node_modules[0], "swagger-ui-dsit", i)));

gulp.task('copy', function() {
    gulp.src(["swagger-ui.css", "favicon-32x32.png", "favicon-16x16.png", "swagger-ui-bundle.js", "swagger-ui-standalone-preset.js"].map(i => path.resolve('.', node_modules[0], "swagger-ui-dist", i)),
             {base: path.resolve('.', node_modules[0], "swagger-ui-dist")})
        .pipe(debug({title: 'copy:'}))
        .pipe(gulp.dest('./target/site/swagger'));
});


//gulp.task('default', ['watch', 'scripts', 'images']);
