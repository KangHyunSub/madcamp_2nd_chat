const express = require('express')
const user = express.Router()
const User = require('../models/userInfo')

//sign up
user.post('/signup', (req, res)=> {

    console.log("/user/signup")
    console.log(req.body)

    const newUser = new User()

    newUser.name = req.body.name
    newUser.phone = req.body.phone
    newUser.photo = req.body.photo
    newUser.desc = req.body.desc
    newUser.address = req.body.address
    newUser.gallery = req.body.gallery

    newUser.save((err, user) =>{
        if (err) {
            console.err(err)
            res.json({result : 0})
            return
        }
        res.json({result : 1})
    })
})


//return all user
user.get('/find', (req, res)=>{
    console.log("/user/find")

    User.find(function(err, users){
        if(err) return res.status(500).send({err : 'database failure'})
        res.json(users)
    })
})

//find user by phone number
user.get('/find/:phone', (req, res)=>{
    
    console.log("/user/find/" + req.params.phone)
    User.find().where('phone').equals(req.params.phone).exec((err, user)=>{
        if(err) return res.status(500).json({error : 'database failure'})
        
        if(!user) return res.json({result : 0})
        res.json(user)
    })

})


module.exports = user