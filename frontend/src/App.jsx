import React, { useEffect, useState } from 'react'
import { api } from './api'
import ContactsList from './components/ContactsList'
import ContactForm from './components/ContactForm'


export default function App() {
const [contacts, setContacts] = useState([])
const [loading, setLoading] = useState(false)
const [error, setError] = useState(null)


const fetchContacts = async () => {
setLoading(true)
try {
const res = await api.get('/contacts')
setContacts(res.data)
} catch (err) {
setError(err.message || 'Erro ao buscar contatos')
} finally {
setLoading(false)
}
}


useEffect(() => { fetchContacts() }, [])


const createContact = async (payload) => {
try {
const res = await api.post('/contacts', payload)
setContacts(prev => [res.data, ...prev])
} catch (err) {
alert('Erro ao criar contato: ' + (err.response?.data?.message || err.message))
}
}


const deleteContact = async (id) => {
if (!confirm('Excluir este contato?')) return
try {
await api.delete(`/contacts/${id}`)
setContacts(prev => prev.filter(c => c.id !== id))
} catch (err) {
alert('Erro ao excluir: ' + (err.message))
}
}


return (
<div className="min-h-screen bg-gray-100 p-6">
<div className="max-w-3xl mx-auto">
<h1 className="text-2xl font-bold mb-4">MetaBusiness — Contatos</h1>


<ContactForm onCreate={createContact} />


<div className="mt-4">
{loading && <div>Carregando...</div>}
{error && <div className="text-red-600">{error}</div>}
<ContactsList contacts={contacts} onDelete={deleteContact} />
</div>
</div>
</div>
)
}