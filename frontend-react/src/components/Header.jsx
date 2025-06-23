import React from 'react'
import Navbar from './Navbar'
import { motion } from "framer-motion"

function Header() {
  return (
    <div className='min-h-screen mb-4 bg-cover bg-center flex 
    items-center w-full overflow-hidden' style=
    {{backgroundImage: "url('/header_img.png')"}} id='Header'>
    <Navbar/>
    <motion.div
    initial={{opacity: 0, y:100}}
    transition={{duration: 1.5}}
    whileInView={{opacity: 1, y:0}}
    viewport={{once: true}}
    className='container text-center mx-auto py-4 px-6 md: px-20 lg:px-42 text-white'>
        <h2 className='text-5xl sm:text-6xl md:text-[82px] inline-block max-w-3xl font-semibold pt-20'>Where art never fades</h2>
        <div className='space-x-6 mt-16'>
        <a href="#Pieces"className="border border-white px-8 py-3 rounded text-white transition duration-300 hover:bg-white hover:text-black hover:scale-105">
        Pieces
        </a>
        <a href="#Contact" className="bg-white px-8 py-3 rounded text-black transition duration-300 hover:bg-black hover:text-white hover:scale-105">
        Share art with Us
        </a>
        </div>
    </motion.div>
    </div>
  )
}

export default Header
