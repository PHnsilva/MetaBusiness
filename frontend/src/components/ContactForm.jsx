import React, { useState } from 'react'


export default function ContactForm({ onCreate }) {
const [name, setName] = useState('')
const [phone, setPhone] = useState('')


const submit = e => {
e.preventDefault()
if (!name.trim() || !phone.trim()) return
onCreate({ name: name.trim(), phone: phone.trim() })
setName('')
setPhone('')
}


return (
<form onSubmit={submit} className="p-4 bg-white rounded shadow flex gap-2">
<input value={name} onChange={e => setName(e.target.value)} placeholder="Nome" className="p-2 border rounded flex-1" />
<input value={phone} onChange={e => setPhone(e.target.value)} placeholder="Telefone" className="p-2 border rounded w-48" />
<button className="px-4 py-2 bg-blue-600 text-white rounded">Adicionar</button>
</form>
)
}