import React from 'react';
import { Routes, Route } from 'react-router-dom';
import TopStrip from './components/TopStrip';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import HomePage from './pages/common/HomePage';

export default function App() {
  return (
    <>
      <TopStrip />
      <Navbar />
      <main style={{ flex: 1 }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          {/* Phase 3+ routes will be wired here */}
          <Route path="*" element={<HomePage />} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}
