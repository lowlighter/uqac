function objective(solution) {
    let x = solution.variables[0], y = solution.variables[1]
    return data.objective.func(x,y);
}


function ackley(x,y) {
    return -20 * Math.exp(-0.2*Math.sqrt(0.5*(x**2 + y**2))) - Math.exp(0.5*(Math.cos(2*Math.PI*x) + Math.cos(2*Math.PI*y))) + Math.E + 20
}

function beale(x,y){
    return (1.5 -x + x*y)**2 + (2.25 - x + (x*y)**2)**2 + (2.625 - x + (x*y)**3)**2
}

function booth(x,y){
    return (x + 2*y - 7)**2 + (2*x + y -5)**2
}

function easom(x,y) {
    return -Math.cos(x)*Math.cos(y)*Math.exp(-((x - Math.PI)**2 + (y - Math.PI)**2))
}

function eggholder(x,y) {
    return -(y +47) * Math.sin(Math.sqrt(Math.abs((x/2)+(y+47)))) - x*Math.sin(Math.sqrt(Math.abs(x-(y+47))))
}

function himmelblau(x,y) {
    return (x**2 +y -11)**2 + (x + y**2 - 7)**2;
}

function holder_table(x,y){
    return -Math.abs(Math.sin(x)*Math.cos(y)*Math.exp(Math.abs(1 - (Math.sqrt(x**2 + y**2)/Math.PI))))
}

function noise_simplex(x, y) {
    return noise.simplex2(x/50, y/50)
}

function noise_perlin(x, y) {
    return noise.perlin2(x/50, y/50)
}

function sphere(x,y) {
    return x**2 + y**2
}


let test_functions = [
    {name:'Ackley', func:ackley, limit:[[-5, 5],[-5, 5]]},
    {name:'Beale ', func:beale, limit:[[-4.5, 4.5],[-4.5, 4.5]]},
    {name:'Booth', func:booth, limit:[[-10, 10],[-10, 10]]},
    {name:'Easom', func:easom, limit:[[-10, 10],[-100, 100]]},
    {name:'Eggholder', func:eggholder, limit:[[-100, 100],[-100, 100]]},
    {name:'Himmelbleau', func:himmelblau, limit:[[-100, 100],[-100, 100]]},
    {name:'Holder Table', func:holder_table, limit:[[-10, 10],[-10, 10]]},
    {name:'Noise (Simplex)', func:noise_simplex, limit:[[-100, 100],[-100, 100]]},
    {name:'Noise (Perlin)', func:noise_perlin, limit:[[-100, 100],[-100, 100]]},
    {name:'Sphere', func:sphere, limit:[[-100, 100],[-100, 100]]}
]