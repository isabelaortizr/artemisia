// src/components/SelectField.jsx
export default function SelectField({ label, name, value, onChange, options }) {
    return (
        <div>
            <label className="block text-gray-700">{label}</label>
            <select
                name={name}
                value={value}
                onChange={onChange}
                className="w-full mt-1 px-3 py-2 border rounded focus:ring-indigo-500 focus:outline-none"
            >
                {options.map(o => (
                    <option key={o} value={o}>{o}</option>
                ))}
            </select>
        </div>
    );
}
