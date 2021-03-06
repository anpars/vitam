import { PipeTransform, Pipe } from '@angular/core';

// FIXME : Put me in some other file
@Pipe({name: 'keys'})
export class KeysPipe implements PipeTransform {
  transform(value, args:string[]) : any {
    let keys = [];
    for (let key in value) {
      keys.push(key);
    }
    return keys;
  }
}

@Pipe({name: 'bytes'})
export class BytesPipe implements PipeTransform {
  transform(value, args:string[]): any {
    if (isNaN(parseFloat(value)) || !isFinite(value)) return '-';
    var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
      number = Math.floor(Math.log(value) / Math.log(1024));
    return (value / Math.pow(1024, Math.floor(number))).toFixed(1) +  ' ' + units[number];
  }
}