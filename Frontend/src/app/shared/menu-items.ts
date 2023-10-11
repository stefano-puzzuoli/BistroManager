import { Injectable } from "@angular/core";


export interface Menu{
    state:string;
    name:string;
    type:string;
    icon:string;
    role:string;
}

const MENUITEMS = [
    {state:'dashboard' , name:'Dashboard', type:'link', icon:'dashboard', role:''},
    {state:'category' , name:'Manage Categories', type:'link', icon:'category', role:'admin'},
    {state:'product' , name:'Manage Products', type:'link', icon:'inventory_2', role:'admin'},
    {state:'order' , name:'Manage Orders', type:'link', icon:'shopping_cart', role:''},
    {state:'bill' , name:'View Bills', type:'link', icon:'backup_table', role:''},
    {state:'user' , name:'Manage Users', type:'link', icon:'people', role:'admin'}

]
@Injectable()
export class MenuItems{
    getMenuitem():Menu[]{
        return MENUITEMS;
    }
}