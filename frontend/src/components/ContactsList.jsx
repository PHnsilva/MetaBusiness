import React from 'react'


export default function ContactsList({ contacts, onDelete }) {
if (!contacts || contacts.length === 0) return (
<div className="p-4">Nenhum contato encontrado</div>
)


return (
<div className="p-4 grid gap-3">
{contacts.map(c => (
<div key={c.id} className="p-3 bg-white rounded shadow flex justify-between items-center">
<div>
<div className="font-semibold">{c.name}</div>
<div className="text-sm text-gray-600">{c.phone}</div>
</div>
<div>
<button onClick={() => onDelete(c.id)} className="px-3 py-1 bg-red-500 text-white rounded">Excluir</button>
</div>
</div>
))}
</div>
)
}