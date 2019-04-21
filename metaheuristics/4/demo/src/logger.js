class Logger {

constructor(){
    this.logs = []
}

log(line){this.logs.unshift({type:'info', text:line})}
error(line){this.logs.unshift({type:'error', text:line})}
success(line){this.logs.unshift({type:'success', text:line})}
warn(line){this.logs.unshift({type:'warn', text:line})}

clear(){this.logs = []}
}